package com.metarhia.lundibundi.console;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.metarhia.lundibundi.console.dagger.HasComponent;
import com.metarhia.lundibundi.console.dagger.components.ActivityComponent;
import com.metarhia.lundibundi.console.dagger.components.ApplicationComponent;
import com.metarhia.lundibundi.console.dagger.components.DaggerActivityComponent;
import com.metarhia.lundibundi.console.dagger.modules.ActivityModule;

import javax.inject.Inject;

/**
 * Created by lundibundi on 7/22/16.
 */
public class MetarhiaActivity extends AppCompatActivity
        implements HasComponent<ActivityComponent> {

    private static final String SCREEN_TAG = "screenTag";

    private ActivityComponent mActivityComponent;
    private MetarhiaScreen mCurrentScreen;

    @Inject MetarhiaBroadcastReceiver mBroadcastReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeInjector();
        mActivityComponent.inject(this);

        mCurrentScreen = (MetarhiaScreen) getSupportFragmentManager().findFragmentByTag(SCREEN_TAG);
    }

    private void initializeInjector() {
        mActivityComponent = DaggerActivityComponent.builder()
                .applicationComponent(getApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();
    }


    public ApplicationComponent getApplicationComponent() {
        return ((MetarhiaApplication) getApplication()).getApplicationComponent();
    }

    public ActivityComponent getActivityComponent() {
        return mActivityComponent;
    }

    @Override
    public ActivityComponent getComponent() {
        return mActivityComponent;
    }

    public void changeScreen(String screenName) {
        changeScreen(screenName, true);
    }

    public void changeScreen(String screenName, boolean create) {
        if (create) mCurrentScreen = MetarhiaScreen.newInstance(screenName);
        // todo ensure activity safety (that this will wait till activity is recreated
        // and will not be called after onSavedInstanceState)

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_main, mCurrentScreen, SCREEN_TAG)
                .addToBackStack(null)
                .commit();
    }

    public MetarhiaScreen getScreen() {
        return mCurrentScreen;
    }

    @Override
    protected void onDestroy() {
        mActivityComponent = null;
        super.onDestroy();
    }

    public void globalInvalidate() {
        if (mCurrentScreen != null) {
            mCurrentScreen.invalidate();
        }
    }
}
