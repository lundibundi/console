package com.metarhia.lundibundi.console.dagger.components;

import android.content.Context;

import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import com.metarhia.jstp.Connection.JSTPConnection;
import com.metarhia.lundibundi.console.MetarhiaTheme;
import com.metarhia.lundibundi.console.NodeEnv;
import com.metarhia.lundibundi.console.MetarhiaApplication;
import com.metarhia.lundibundi.console.NotificationHandler;
import com.metarhia.lundibundi.console.dagger.modules.ApplicationModule;
import com.metarhia.lundibundi.console.dagger.modules.JSTPConnectionModule;

import com.metarhia.lundibundi.console.utils.Constants;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by lundibundi on 7/22/16.
 */
@Singleton
@Component(modules = {ApplicationModule.class, JSTPConnectionModule.class})
public interface ApplicationComponent {
    void inject(MetarhiaApplication activity);

    JSTPConnection jstpConnection();

    @Named(Constants.APPLICATION_CONTEXT)
    Context provideApplicationContext();

    MetarhiaApplication provideMetarhiaApplication();

    MetarhiaTheme provideApplicationTheme();

    @Named(Constants.APPLICATION_NAME)
    String provideApplicationName();

    @Named(Constants.GLOBAL_BROADCAST)
    LocalBroadcastManager provideBroadcastManager();

    @Named(Constants.NODE_GLOBAL)
    NodeEnv provideGlobalNodeEnv();

    @Named(Constants.DEFAULT_PREFS)
    SharedPreferences provideSharedPrefs();

    Resources provideResources();

    @Named(Constants.MAIN_HANDLER)
    Handler provideMainHandler();

    void inject(NotificationHandler notificationHandler);

    void inject(ApplicationModule applicationModule);
}
