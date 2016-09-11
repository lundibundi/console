package com.metarhia.lundibundi.console;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import com.eclipsesource.v8.NodeJS;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Function;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.lundibundi.console.utils.JSUtils;
import com.metarhia.lundibundi.console.utils.Utils;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RecursiveAction;

import static com.metarhia.lundibundi.console.MetarhiaObjectUtils.*;

/**
 * Created by lundibundi on 7/23/16.
 */
public class NodeEnv {

    public static final String API_CONF_KEY = "api";
    public static final String APP_CONF_KEY = "app";
    public static final String UTILS_CONF_KEY = "utils";
    public static final String SCREEN_CONF_KEY = "screens";
    public static final String CONTROLS_CONF_KEY = "controls";
    public static final String PROTO_CONF_KEY = "proto";
    public static final String DEFAULT_PROPS_CONF_KEY = "props";
    public static final String CONTROL_TYPE_KEY = "control";

    private static final String API_PREFIX = API_CONF_KEY;
    private static final String APP_PREFIX = API_PREFIX + "." + APP_CONF_KEY;
    private static final String UTILS_PREFIX = API_PREFIX + "." + UTILS_CONF_KEY;
    private static final String INTERNALS_PREFIX = APP_PREFIX + "." + "internal";
    private static final String CACHE_PREFIX = INTERNALS_PREFIX + "." + "cache";

    private NodeJS mNodeJS;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private Map<String, String> mRegisteredUtilFunctions;

    private Map<String, List<Long>> mRegisteredApiFunctionIds;

    @Inject
    public NodeEnv(final String name) {
        mRegisteredApiFunctionIds = new HashMap<>();

        mHandlerThread = new HandlerThread("Node thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                initNode(name);
            }
        });
    }

    private void initNode(String name) {
        mNodeJS = NodeJS.createNodeJS(name, null);
        V8Function require = mNodeJS.getRequire();
        V8 runtime = mNodeJS.getRuntime();
        runtime.add("require", require.twin());
        require.release();

        resetJSRuntime(runtime);
    }

    private void resetJSRuntime(V8 runtime) {
        initJSObjects(false, API_PREFIX, APP_PREFIX, UTILS_PREFIX,
            INTERNALS_PREFIX, CACHE_PREFIX);

        mRegisteredUtilFunctions = JSUtils.addJSUtils(runtime, UTILS_PREFIX);
    }

    private void initJSObjects(boolean override, String... objects) {
        V8 runtime = mNodeJS.getRuntime();
        for (String obj : objects) {
            if (override || JSUtils.isUndefined(runtime, obj)) {
                runtime.executeVoidScript(obj + " = {}");
            }
        }
    }

    private StringBuilder initJSHierarchy(String prefix, String... hierarchy) {
        V8 runtime = mNodeJS.getRuntime();
        StringBuilder builder = new StringBuilder(prefix);
        int i = 0;
        while (i < hierarchy.length && !JSUtils.isUndefined(runtime, builder.toString())) {
            builder.append(".")
                .append(hierarchy[i]);
            ++i;
        }

        runtime.executeVoidScript(builder.toString() + " = {}");

        for (; i < hierarchy.length; i++) {
            builder.append(".")
                .append(hierarchy[i]);
            runtime.executeVoidScript(builder.toString() + " = {}");
        }

        return builder;
    }

    public static <T> Utils.SubCompletableFuture<V8Object, Map<String, ?>> getConfigurationDecoded(
        Utils.SubCompletableFuture<T, V8Object> future) {
        return future.thenApply(new Utils.Function<V8Object, Map<String, ?>>() {
            @Override
            public Map<String, ?> apply(V8Object config) throws Exception {
                Map<String, ? super Object> map = V8ObjectUtils.toMap(config);
                config.release();
                return map;
            }
        });
    }

    public <T> Utils.SubCompletableFuture<T, Map<String, ?>> getDecoded(final V8Object object) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Map<String, ?>>() {
            @Override
            public Map<String, ?> call() throws Exception {
                final Map<String, ? super Object> map = V8ObjectUtils.toMap(object);
                object.release();
                return map;
            }
        });
    }


//    public V8Object getAppApi(String... namespaces) {
//        V8 runtime = mNodeJS.getRuntime();
//        StringBuilder builder = composeAppAccessExpr(namespaces);
//        Object api = runtime.executeScript(builder.toString());
//        if (api instanceof V8Object) {
//            return (V8Object) api;
//        }
//        return null;
//    }

//    public void getAppApiParsed(final Utils.RunnableWithParams<Map<String, ?>> callback, final String... namespaces) {
//        getAppApiParsed(callback, mHandler, namespaces);
//    }
//
//    public void getAppApiParsed(final Utils.RunnableWithParams<Map<String, ?>> callback, final Handler handler, final String... namespaces) {
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                V8Object api = getAppApi(namespaces);
//                final Map<String, ? super Object> apiMap = V8ObjectUtils.toMap(api);
//                callback.setParameters(apiMap);
//                handler.post(callback);
//                api.release();
//            }
//        });
//    }


    public void addAppConfiguration() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                addDummyAppApi();
            }
        });
    }

    private void addDummyAppApi() {
        String createJsObjectUtil = mRegisteredUtilFunctions.get(JSUtils.CREATE_OBJECT_FUNCTION);

        String appProto = getObjectProto(MetarhiaApplication.class);
        String appDefaults = getObjectDefaults(MetarhiaApplication.contractClass);

        StringBuilder jsAppConfiguration = new StringBuilder(APP_PREFIX);
        String createObjectCall = JSUtils.composeFunctionCall(createJsObjectUtil, appProto, appDefaults, APP_PREFIX);
        jsAppConfiguration.append(" = ")
            .append(createObjectCall);

        mNodeJS.getRuntime().executeVoidScript(jsAppConfiguration.toString());
    }

    public void addScreenConfiguration(final String screenName, final JSObject screenConf) {
        addDummyScreenApi(screenName, screenConf);
    }

    public void addScreenConfigurationAsync(final String screenName, final JSObject screenConf) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                addScreenConfiguration(screenName, screenConf);
            }
        });
    }

    public Utils.SubCompletableFuture<Void, V8Object> getScreenConfigurationAsync(final String screenName) {
        return new Utils.SubCompletableFuture<>(
            mHandler, new Callable<V8Object>() {
            @Override
            public V8Object call() throws Exception {
                return getScreenConfiguration(screenName);
            }
        });
    }

    public V8Object getScreenConfiguration(String screenName) {
        return getConfiguration(screenName);
    }

    public void setScreensConfiguration(JSObject screens) {
        for (Map.Entry<String, JSValue> me : screens.getValue().entrySet()) {
            addScreenConfiguration(me.getKey(), (JSObject) me.getValue());
        }
    }

    public void setScreensConfigurationAsync(final JSObject screens) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setScreensConfiguration(screens);
            }
        });
    }

    private void addDummyScreenApi(String screenName, JSObject screenConf) {
        addDummyControlApi(screenName, screenConf);

        final JSObject screenControls = (JSObject) screenConf.get(CONTROLS_CONF_KEY);
        for (Map.Entry<String, JSValue> control : screenControls.getValue().entrySet()) {
            addControlConfiguration(screenName, control.getKey(), (JSObject) control.getValue());
        }
    }

    public void addControlConfigurationAsync(final String screenName, final String controlName,
                                             final JSObject controlConf) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                addControlConfigurationAsync(screenName, controlName, controlConf);
            }
        });
    }

    public void addControlConfiguration(String screenName, String controlName, JSObject controlConf) {
        final String controlPath = composeAccessExpr(screenName, CONTROLS_CONF_KEY, controlName).toString();
        addDummyControlApi(controlPath, controlConf);
    }

    public Utils.SubCompletableFuture<Void, V8Object> getControlConfigurationAsync(
        final String screenName, final String controlName) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<V8Object>() {
            @Override
            public V8Object call() throws Exception {
                return getControlConfiguration(screenName, controlName);
            }
        });
    }

    public V8Object getControlConfiguration(String screenName, String controlName) {
        return getConfiguration(screenName, CONTROLS_CONF_KEY, controlName);
    }

    private void addDummyControlApi(String controlPath, JSObject controlConfObject) {
        String controlType = (String) controlConfObject.get(CONTROL_TYPE_KEY).getGeneralizedValue();
        String controlConf = controlConfObject.toString();
        addDummyControlApi(controlPath, controlType, controlConf);
    }

    private void addDummyControlApi(String controlPath, String controlType, String controlConf) {
        String createJsObjectUtil = mRegisteredUtilFunctions.get(JSUtils.CREATE_OBJECT_FUNCTION);

        String controlDefaults = getControlDefaultsCached(controlType);
        String controlProto = getControlProtoCached(controlType);

        StringBuilder jsControlConfiguration = composeAppAccessExpr(controlPath);
        String createObjectCall = JSUtils.composeFunctionCall(createJsObjectUtil, controlProto, controlDefaults, controlConf);
        jsControlConfiguration.append(" = ")
            .append(createObjectCall);

        mNodeJS.getRuntime().executeScript(jsControlConfiguration.toString());
    }


    private String getControlDefaultsCached(final String controlType) {
        return getCachedValue(new Utils.ResultRunnable<String>() {
            @Override
            public void run() {
                setResult(MetarhiaViewUtils.getControlDefaults(controlType));
            }
        }, false, DEFAULT_PROPS_CONF_KEY, controlType);
    }

    private String getControlProtoCached(final String controlType) {
        return getCachedValue(new Utils.ResultRunnable<String>() {
            @Override
            public void run() {
                setResult(MetarhiaViewUtils.getControlProto(controlType));
            }
        }, false, PROTO_CONF_KEY, controlType);
    }

    private String getCachedValue(Utils.ResultRunnable<String> provider, boolean override, String cacheKey, String... cacheNamespaces) {
        final V8 runtime = mNodeJS.getRuntime();
        final StringBuilder valuePathBuilder = initJSHierarchy(CACHE_PREFIX, cacheNamespaces)
            .append(".")
            .append(cacheKey);

        String valuePath = valuePathBuilder.toString();
        if (override || JSUtils.isUndefined(runtime, valuePath)) {
            provider.run();

            valuePathBuilder.append(" = ")
                .append(provider.getResult());
            runtime.executeVoidScript(valuePathBuilder.toString());
        }
        return valuePath;

    }

    public Utils.SubCompletableFuture<Void, V8Object> getConfigurationAsync(final String... namespaces) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<V8Object>() {
            @Override
            public V8Object call() throws Exception {
                return getConfiguration(namespaces);
            }
        });
    }

    public V8Object getConfiguration(String... namespaces) {
        V8 runtime = mNodeJS.getRuntime();
        String accessor = composeAppAccessExpr(namespaces).toString();
        Object result = runtime.executeScript(accessor);
        if (result instanceof V8Object) {
            return (V8Object) result;
        }
        return null;
    }

    @NonNull
    public static StringBuilder composeAppAccessExpr(String... namespaces) {
        StringBuilder builder = new StringBuilder(APP_PREFIX);
        for (String name : namespaces) {
            builder.append(".")
                .append(name);
        }
        return builder;
    }

    @NonNull
    public static StringBuilder composeAccessExpr(String... namespaces) {
        StringBuilder builder = new StringBuilder();
        for (String name : namespaces) {
            builder.append(name)
                .append(".");
        }
        if (builder.length() > 1) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder;
    }

    public NodeJS getNodeJS() {
        return mNodeJS;
    }

    public V8 getRuntime() {
        return mNodeJS.getRuntime();
    }

    public Map<String, String> getRegisteredUtilFunctions() {
        return mRegisteredUtilFunctions;
    }

    public Utils.SubCompletableFuture<Void, Boolean> isUndefinedAsync(final String... namespaces) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isUndefined(namespaces);
            }
        });
    }

    public boolean isUndefined(String... namespaces) {
        return JSUtils.isUndefined(mNodeJS.getRuntime(), namespaces);
    }

    public Utils.SubCompletableFuture<Void, Boolean> isAppUndefinedAsync(final String... namespaces) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return isAppUndefined(namespaces);
            }
        });
    }

    public boolean isAppUndefined(String... namespaces) {
        return JSUtils.isAppUndefined(mNodeJS.getRuntime(), namespaces);
    }

    public Utils.SubCompletableFuture<Void, Void> registerAppApiAsync(final MetarhiaApplication metarhiaApplication, final Set<FunctionConf> availableApiMethods) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                registerAppApi(metarhiaApplication, availableApiMethods);
                return null;
            }
        });
    }

    public void registerAppApi(MetarhiaApplication metarhiaApplication, Set<FunctionConf> availableApiMethods) {
        List<Long> methodIds = MetarhiaObjectUtils.registerApi(
            mNodeJS.getRuntime(), metarhiaApplication, APP_PREFIX, availableApiMethods);
        mRegisteredApiFunctionIds.put(APP_PREFIX, methodIds);
    }

    public Utils.SubCompletableFuture<Void, Void> unregisterAppApiAsync(final Set<FunctionConf> availableApiMethods) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                unregisterAppApi(availableApiMethods);
                return null;
            }
        });
    }

    public void unregisterAppApi(Set<FunctionConf> availableApiMethods) {
        unregisterApi(availableApiMethods);
    }

    public Utils.SubCompletableFuture<Void, Void> unregisterApiAsync(final Set<FunctionConf> availableApiMethods, final String... namespaces) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                unregisterApi(availableApiMethods, namespaces);
                return null;
            }
        });
    }

    public void unregisterApi(Set<FunctionConf> availableApiMethods, String... namespaces) {
        String accessPrefix = composeAppAccessExpr(namespaces).toString();
        List<Long> methodIds = mRegisteredApiFunctionIds.get(accessPrefix);
        if (methodIds != null) {
            MetarhiaObjectUtils.unregisterApi(mNodeJS.getRuntime(), methodIds, accessPrefix, availableApiMethods);
        }
    }

    public Utils.SubCompletableFuture<Void, Void> registerScreenApiAsync(
        final MetarhiaScreen metarhiaScreen, final Set<FunctionConf> availableApiMethods) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                registerScreenApi(metarhiaScreen, availableApiMethods);
                return null;
            }
        });
    }

    public void registerScreenApi(MetarhiaScreen metarhiaScreen, Set<FunctionConf> availableApiMethods) {
        String accessExpr = composeAppAccessExpr(metarhiaScreen.getJSName()).toString();
        List<Long> methodIds = MetarhiaObjectUtils.registerApi(mNodeJS.getRuntime(), metarhiaScreen, accessExpr, availableApiMethods);
        mRegisteredApiFunctionIds.put(accessExpr, methodIds);
    }

    public Utils.SubCompletableFuture<Void, Void> unregisterScreenApiAsync(
        final String screenName, final Set<FunctionConf> availableApiMethods) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                unregisterScreenApi(screenName, availableApiMethods);
                return null;
            }
        });
    }

    public void unregisterScreenApi(String screenName, Set<FunctionConf> availableApiMethods) {
        unregisterApi(availableApiMethods, screenName);
    }

    public Utils.SubCompletableFuture<Void, Void> registerControlApiAsync(
        final String screenName, final MetarhiaControl metarhiaControl, final Set<FunctionConf> availableApiMethods) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                registerControlApi(screenName, metarhiaControl, availableApiMethods);
                return null;
            }
        });
    }

    public void registerControlApi(String screenName, MetarhiaControl metarhiaControl, Set<FunctionConf> availableApiMethods) {
        String accessExpr = composeAppAccessExpr(screenName, CONTROLS_CONF_KEY, metarhiaControl.getJSName()).toString();
        final List<Long> methodIds = MetarhiaObjectUtils.registerApi(
            mNodeJS.getRuntime(), metarhiaControl, accessExpr, availableApiMethods);
        mRegisteredApiFunctionIds.put(accessExpr, methodIds);
    }

    public Utils.SubCompletableFuture<Void, Void> unregisterControlApiAsync(
        final String screenName, final String name, final Set<FunctionConf> availableApiMethods) {
        return new Utils.SubCompletableFuture<>(mHandler, new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                unregisterControlApi(screenName, name, availableApiMethods);
                return null;
            }
        });
    }

    public void unregisterControlApi(String screenName, String name, Set<FunctionConf> availableApiMethods) {
        unregisterApi(availableApiMethods, screenName, CONTROLS_CONF_KEY, name);
    }

//
//    private static class ControlConf {
//        public String prototypePath;
//        public String propertiesPath;
//
//        public ControlConf(String prototypePath, String propertiesPath) {
//            this.prototypePath = prototypePath;
//            this.propertiesPath = propertiesPath;
//        }
//    }

}
