package com.metarhia.lundibundi.console.dagger.components;

import android.content.Context;

import com.metarhia.lundibundi.console.MetarhiaButton;
import com.metarhia.lundibundi.console.MetarhiaEdit;
import com.metarhia.lundibundi.console.MetarhiaLabel;
import com.metarhia.lundibundi.console.MetarhiaScreen;
import com.metarhia.lundibundi.console.MetarhiaActivity;
import com.metarhia.lundibundi.console.dagger.modules.ActivityModule;
import com.metarhia.lundibundi.console.dagger.utils.PerActivity;

import com.metarhia.lundibundi.console.utils.Constants;
import dagger.Component;

import javax.inject.Named;

/**
 * Created by lundibundi on 7/22/16.
 */
@PerActivity
@Component(dependencies = {ApplicationComponent.class}, modules = {ActivityModule.class})
public interface ActivityComponent extends ApplicationComponent {
    MetarhiaActivity metarhiaActivity();

    @Named(Constants.ACTIVITY_CONTEXT)
    Context activityContext();

    void inject(MetarhiaActivity metarhiaActivity);

    void inject(MetarhiaScreen metarhiaScreen);

    void inject(MetarhiaEdit metarhiaEdit);

    void inject(MetarhiaButton metarhiaButton);

    void inject(MetarhiaLabel metarhiaLabel);
}
