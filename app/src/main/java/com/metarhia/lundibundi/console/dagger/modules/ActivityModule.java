package com.metarhia.lundibundi.console.dagger.modules;

import android.content.Context;

import com.metarhia.lundibundi.console.DeviceConfiguration;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.MetarhiaActivity;
import com.metarhia.lundibundi.console.dagger.utils.PerActivity;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * Created by lundibundi on 7/22/16.
 */
@Module
public class ActivityModule {
    private MetarhiaActivity mMetarhiaActivity;

    public ActivityModule(MetarhiaActivity metarhiaActivity) {
        mMetarhiaActivity = metarhiaActivity;
    }

    @Provides
    @PerActivity
    public MetarhiaActivity provideActivity() {
        return mMetarhiaActivity;
    }

    @Provides
    @Named(Constants.ACTIVITY_CONTEXT)
    @PerActivity
    public Context provideContext() {
        return mMetarhiaActivity;
    }

    @Provides
    @PerActivity
    public DeviceConfiguration provideDeviceConfiguration() {
        return new DeviceConfiguration();
    }
}
