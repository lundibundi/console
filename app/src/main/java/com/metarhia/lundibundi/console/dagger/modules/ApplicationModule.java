package com.metarhia.lundibundi.console.dagger.modules;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;

import com.metarhia.lundibundi.console.MetarhiaTheme;
import com.metarhia.lundibundi.console.NodeEnv;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.MetarhiaApplication;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lundibundi on 7/22/16.
 */
@Module
public class ApplicationModule {

    private final MetarhiaApplication mMetarhiaApplication;

    public ApplicationModule(MetarhiaApplication metarhiaApplication) {
        mMetarhiaApplication = metarhiaApplication;
    }

    @Provides
    @Named(Constants.APPLICATION_CONTEXT)
    @Singleton
    public Context provideApplicationContext() {
        return mMetarhiaApplication.getApplicationContext();
    }

    @Provides
    @Singleton
    public MetarhiaApplication provideMetarhiaApplication() {
        return mMetarhiaApplication;
    }

    @Provides
    @Singleton
    public MetarhiaTheme provideApplicationTheme() {
        return mMetarhiaApplication.getMetarhiaTheme();
    }

    @Provides
    @Named(Constants.APPLICATION_NAME)
    @Singleton
    public String provideApplicationName() {
        return mMetarhiaApplication.getApplicationName();
    }

    @Provides
    @Named(Constants.GLOBAL_BROADCAST)
    @Singleton
    public LocalBroadcastManager provideBroadcastManager() {
        return LocalBroadcastManager.getInstance(mMetarhiaApplication);
    }

    @Provides
    @Named(Constants.NODE_GLOBAL)
    @Singleton
    public NodeEnv provideGlobalNodeEnv() {
        return new NodeEnv("global");
    }

    @Provides
    @Named(Constants.DEFAULT_PREFS)
    @Singleton
    public SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mMetarhiaApplication);
    }

    @Provides
    @Singleton
    public Resources provideResources() {
        return mMetarhiaApplication.getResources();
    }

    @Provides
    @Named(Constants.MAIN_HANDLER)
    @Singleton
    public Handler provideMainHandler() {
        return new Handler(mMetarhiaApplication.getMainLooper());
    }
}
