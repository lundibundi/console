package com.metarhia.lundibundi.console.dagger.components;

import android.view.View;

import com.metarhia.lundibundi.console.MetarhiaViewFactory;
import com.metarhia.lundibundi.console.dagger.modules.ActivityModule;
import com.metarhia.lundibundi.console.dagger.utils.PerActivity;

import com.metarhia.lundibundi.console.dagger.utils.PerActivitySub;
import dagger.Component;

/**
 * Created by lundibundi on 7/23/16.
 */
@PerActivitySub
@Component(dependencies = {ActivityComponent.class})
public interface MetarhiaViewFactoryComponent {
    MetarhiaViewFactory factory();
}
