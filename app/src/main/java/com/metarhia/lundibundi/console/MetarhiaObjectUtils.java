package com.metarhia.lundibundi.console;

import android.os.Handler;
import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSRaw;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.lundibundi.console.contracts.MetarhiaContractUtils;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.utils.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static com.metarhia.lundibundi.console.MetarhiaViewUtils.processPostUpdate;

/**
 * Created by lundibundi on 8/29/16.
 */
public class MetarhiaObjectUtils {

    private MetarhiaObjectUtils() {
    }

    public static final Map<String, Class<? extends MetarhiaObjectFactory>> metarhiaObjectHierarchies = new HashMap<>();

    static {
        metarhiaObjectHierarchies.put(Constants.HIERARCHY_METARHIA_CONTROL, MetarhiaViewFactory.class);
    }

    public static List<Long> registerApi(V8 v8, Object receiver, String accessPrefix, Set<FunctionConf> apiFunctions) {
        V8Object subject = v8.executeObjectScript(accessPrefix);
        List<Long> apiIDs = new LinkedList<>();
        for (FunctionConf func : apiFunctions) {
            final long methodId = subject.explicitRegisterJavaMethod(
                receiver, func.methodName, func.jsName, func.params);
            apiIDs.add(methodId);
        }
        subject.release();
        return apiIDs;
    }

    public static void unregisterApi(V8 v8, List<Long> methodIDs, String accessPrefix, Set<FunctionConf> apiFunctions) {
        for (Long id : methodIDs) {
            v8.unregisterMethodDescriptor(id);
        }
        for (FunctionConf func : apiFunctions) {
            String defaultFunc = NodeEnv.composeAccessExpr(accessPrefix, func.jsName)
                .append(" = ")
                .append(func.defaultValue)
                .toString();
            v8.executeVoidScript(defaultFunc);
        }
    }

    public static Class getContractClass(Class<? extends MetarhiaObject> clazz) {
        try {
            return (Class) clazz.getDeclaredField("contractClass").get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Set<FunctionConf> getAvailableApiMethods(Class<? extends MetarhiaObject> clazz) {
        try {
            return (Set<FunctionConf>) clazz.getDeclaredField("availableApiMethods").get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getObjectProto(Class<? extends MetarhiaObject> objectClass) {
        final Set<FunctionConf> availableApiMethods = getAvailableApiMethods(objectClass);
        JSObject proto = new JSObject();
        for (final FunctionConf fc : availableApiMethods) {
            proto.put(fc.jsName, new JSRaw(fc.defaultValue));
        }
        return proto.toString();
    }

    public static Utils.SubCompletableFuture<V8Object, Map<String, ?>> getConfigurationDecoded(
        NodeEnv nodeEnv, final String... namespaces) {
        return nodeEnv.getConfigurationAsync(namespaces)
            .thenApply(new Utils.Function<V8Object, Map<String, ?>>() {
                @Override
                public Map<String, ?> apply(V8Object config) throws Exception {
                    return V8ObjectUtils.toMap(config);
                }
            });
    }

    public static String getObjectDefaultsFromClass(Class objectClass) {
        final Class contractClass = getContractClass(objectClass);
        if (contractClass != null) {
            return getObjectDefaults(contractClass);
        }
        return "";
    }

    public static String getObjectDefaults(Class contractClass) {
        List<Class<?>> appliableContracts = MetarhiaContractUtils.getSortedAppliableContracts(contractClass);
        if (appliableContracts == null) return "";

        Map<String, String> defaultConfiguration = MetarhiaContractUtils.generateDefaultConfiguration(appliableContracts);
        JSObject defaults = new JSObject();
        for (Map.Entry<String, String> me : defaultConfiguration.entrySet()) {
            JSObject property = new JSObject();
            property.put("writable", true)
                .put("configurable", true)
                .put("enumerable", true)
                .put("value", me.getValue());
            defaults.put(me.getKey(), property);
        }
        return defaults.toString();
    }

    // todo run in separate thread, postponed until proper threading model is implemented
    public static void updateConfiguration(final MetarhiaObject metarhiaObject, final V8Object configuration) {
        final Map<String, ?> confMap = V8ObjectUtils.toMap(configuration);
        configuration.release();
        metarhiaObject.updateConfiguration(confMap);
    }

    public static void updateConfiguration(final MetarhiaObject metarhiaObject, Map<String, ?> configuration,
                                           List<Runnable> postUpdateActions, Class contract, Class subject) {
        try {
            // get update method
            Method updateMethod = contract.getDeclaredMethod("updateValue", subject, String.class, Object.class);

            for (Map.Entry<String, ?> me : configuration.entrySet()) {
                updateMethod.invoke(null, metarhiaObject, me.getKey(), me.getValue());
            }

            processPostUpdate(postUpdateActions);
            postUpdateActions.clear();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static <T> void postUpdateConfiguration(final NodeEnv nodeEnv, final Handler updaterHandler,
                                                   final MetarhiaObject object,
                                                   Utils.SubCompletableFuture<T, V8Object> future) {
        final Utils.SubCompletableFuture<V8Object, Map<String, ?>> nextFuture =
            NodeEnv.getConfigurationDecoded(future);
        postUpdateConfigurationReal(nodeEnv, updaterHandler, object, nextFuture);
    }

    public static void postUpdateConfiguration(NodeEnv nodeEnv, Handler updaterHandler,
                                               final MetarhiaObject object, V8Object configuration) {
        postUpdateConfigurationReal(nodeEnv, updaterHandler, object, nodeEnv.getDecoded(configuration));
    }

    public static <T> void postUpdateConfigurationReal(NodeEnv nodeEnv, Handler updaterHandler,
                                                       final MetarhiaObject object,
                                                       Utils.SubCompletableFuture<T, Map<String, ?>> future) {
        future.thenApply(updaterHandler, new Utils.Function<Map<String, ?>, Void>() {
            @Override
            public Void apply(Map<String, ?> confMap) throws Exception {
                object.updateConfiguration(confMap);
                return null;
            }
        }).start();
    }

    public static class FunctionConf {
        public static final String defaultFunction = "function() {}";

        public String jsName;
        public String defaultValue;
        public String methodName;
        public Class<?>[] params;

        public FunctionConf(String methodName, String jsName, Class<?>... params) {
            this(methodName, jsName, defaultFunction, params);
        }

        public FunctionConf(String methodName, String jsName, String defaultValue, Class<?>... params) {
            this.methodName = methodName;
            this.jsName = jsName;
            this.defaultValue = defaultValue;
            this.params = params;
        }
    }
}
