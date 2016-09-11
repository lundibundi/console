package com.metarhia.lundibundi.console;

import android.app.Application;
import android.content.Intent;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.eclipsesource.v8.V8Object;
import com.eclipsesource.v8.utils.V8ObjectUtils;
import com.metarhia.console.compiler.annotations.ApiMethod;
import com.metarhia.jstp.Connection.JSCallback;
import com.metarhia.jstp.Connection.JSTPConnection;
import com.metarhia.jstp.core.Handlers.ManualHandler;
import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.jstp.core.JSTypes.JSObject;
import com.metarhia.jstp.core.JSTypes.JSString;
import com.metarhia.jstp.core.JSTypes.JSValue;
import com.metarhia.lundibundi.console.contracts.MetarhiaAppContract;
import com.metarhia.lundibundi.console.dagger.components.ApplicationComponent;
import com.metarhia.lundibundi.console.dagger.components.DaggerApplicationComponent;
import com.metarhia.lundibundi.console.dagger.modules.ApplicationModule;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.utils.Utils;
import com.metarhia.lundibundi.console.utils.jstphandlers.AppConfigurationHandler;
import com.metarhia.lundibundi.console.utils.jstphandlers.JSTPAppConfigurationHandler;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import static com.metarhia.lundibundi.console.MetarhiaObjectUtils.*;

/**
 * Created by lundibundi on 7/22/16.
 */
@com.metarhia.console.compiler.annotations.MetarhiaObject(
    contracts = {MetarhiaAppContract.class})
public class MetarhiaApplication extends Application implements
    MetarhiaObject,
    AppConfigurationHandler {

    private static final String LOG_TAG = MetarhiaApplication.class.getSimpleName();

    public static final String APPLICATION_NAME = "ConsoleTest";
    public static final String INTERFACE_NAME = "consoleTest";

    public static final String DATA_SCREEN_NAME = "screenName";


    public static final Class contractClass = MetarhiaAppContract.class;

    public static final Set<FunctionConf> availableApiMethods = new HashSet<>();

    static {
        availableApiMethods.add(new FunctionConf("changeScreen", "changeScreen", String.class));
        availableApiMethods.add(new FunctionConf("updateConfiguration", "updateConfiguration", V8Object.class));
        availableApiMethods.add(new FunctionConf("invalidateApp", "invalidate"));
        availableApiMethods.add(new FunctionConf("requestAppConfiguration", "requestConfiguration"));
    }

    private ApplicationComponent mApplicationComponent;

    @Inject @Named(Constants.MAIN_HANDLER) Handler mMainHandler;

    @Inject JSTPConnection mConnection;

    @Inject @Named(Constants.NODE_GLOBAL) NodeEnv mNodeEnv;

    @Inject @Named(Constants.GLOBAL_BROADCAST) LocalBroadcastManager mBroadcastManager;

    @Inject NotificationHandler mNotificationHandler;

    private String mApplicationName;

    private MetarhiaTheme mMetarhiaTheme;

    private JSTPAppConfigurationHandler mJSTPAppConfigurationHandler;

    private List<Runnable> mPostUpdateActions;

    @Override
    public void onCreate() {
        mApplicationName = "default";
        mMetarhiaTheme = new MetarhiaTheme(this, "default");
        mJSTPAppConfigurationHandler = new JSTPAppConfigurationHandler(this);
        mPostUpdateActions = new LinkedList<>();


        initializeInjector();
        mApplicationComponent.inject(this);

        mNodeEnv.addAppConfiguration();
        mNodeEnv.registerAppApiAsync(this, availableApiMethods);
        mConnection.addEventHandler(INTERFACE_NAME, new ManualHandler() {
            @Override
            public void invoke(JSValue packet) {
                // handle screen change event
                JSString screenName = (JSString) ((JSObject) packet).get("changeScreen");
                if (screenName != null) {
                    changeScreen(screenName.getValue());
                }
            }
        });
        setUpConfiguration();

        super.onCreate();
    }

    private void initializeInjector() {
        mApplicationComponent = DaggerApplicationComponent.builder()
            .applicationModule(new ApplicationModule(this))
            .build();
    }

    public ApplicationComponent getApplicationComponent() {
        return mApplicationComponent;
    }


    public void setUpConfiguration() {
        mConnection.handshake(APPLICATION_NAME, new ManualHandler() {
            @Override
            public void invoke(JSValue packet) {
                getApplicationConfiguration();
            }
        });
    }

    private void getApplicationConfiguration() {
        requestAppConfiguration();
    }

    @Override
    public void onConfiguration(String appName, final JSObject screens, String themeName) {
        mApplicationName = appName;
        mMetarhiaTheme = new MetarhiaTheme(getApplicationContext(), themeName);

        // add configuration to js
        Handler mainHandler = new Handler(this.getMainLooper());
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                mNodeEnv.setScreensConfigurationAsync(screens);

                final Intent intent = new Intent(MetarhiaApplication.this, MetarhiaActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
//        BroadcastEvents.sendInvalidationEvent(mBroadcastManager);
    }

    public String getApplicationName() {
        return mApplicationName;
    }

    public MetarhiaTheme getMetarhiaTheme() {
        return mMetarhiaTheme;
    }

    public LocalBroadcastManager getBroadcastManager() {
        return mBroadcastManager;
    }

    @Override
    public void updateConfiguration(Map<String, ?> configuration) {
        MetarhiaObjectUtils.updateConfiguration(this, configuration, mPostUpdateActions,
            MetarhiaAppContract.class, MetarhiaApplication.class);
    }

    @ApiMethod
    public void updateConfiguration(final V8Object configuration) {
        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this, configuration);
    }

    public void updateConfiguration() {
        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this,
            mNodeEnv.getConfigurationAsync());
    }

    @Override
    public void addPostUpdateAction(Runnable action) {
        mPostUpdateActions.add(action);
    }

    @ApiMethod("invalidate")
    public void invalidateApp() {
        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this,
            mNodeEnv.getConfigurationAsync());

        BroadcastEvents.sendInvalidationEvent(mBroadcastManager);
    }

    @ApiMethod("requestConfiguration")
    public void requestAppConfiguration() {
        mConnection.call(INTERFACE_NAME, "startup", new JSArray(), mJSTPAppConfigurationHandler);
    }

    @ApiMethod
    public void changeScreen(final String screenName) {
        mNodeEnv.isAppUndefinedAsync(screenName)
            .thenApply(new Utils.Function<Boolean, Void>() {
                @Override
                public Void apply(Boolean undefined) throws Exception {
                    if (undefined) {
                        requestScreenConfiguration(screenName);
                    } else {
                        BroadcastEvents.changeScreenEvent(mBroadcastManager, DATA_SCREEN_NAME, screenName);
                    }
                    return null;
                }
            })
            .start();
    }

    private void requestScreenConfiguration(final String screenName) {
        JSArray args = new JSArray();
        args.add(screenName);

        mConnection.call(INTERFACE_NAME, "requestScreenConfiguration", args, new ManualHandler() {
            @Override
            public void invoke(JSValue value) {
                JSObject response = (JSObject) value;
                if (response.containsKey(JSCallback.OK.toString())) {
                    final JSObject screenConfiguration = (JSObject) response.get(1);
                    mNodeEnv.addScreenConfigurationAsync(screenName, screenConfiguration);
                    BroadcastEvents.changeScreenEvent(mBroadcastManager, DATA_SCREEN_NAME, screenName);
                } else if (response.containsKey(JSCallback.ERROR.toString())) {
                    Log.i(LOG_TAG, "Requested non existent Screen: " + screenName);
                }
            }
        });
    }

    public void setName(String name) {
        mApplicationName = name;
    }

    @Override
    public String getJSName() {
        return mApplicationName;
    }

    @Override
    public MetarhiaObject getMetarhiaParent() {
        return null;
    }

    @Override
    public void setMetarhiaParent(MetarhiaObject parent) {
        // nothing
    }

    public Handler getMainHandler() {
        return mMainHandler;
    }
}
