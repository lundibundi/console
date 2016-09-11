package com.metarhia.lundibundi.console;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.metarhia.lundibundi.console.utils.Constants;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by lundibundi on 7/25/16.
 */
public class MetarhiaBroadcastReceiver {

    private LocalBroadcastManager mBroadcastManager;
    private MetarhiaActivity mActivity;

    private BroadcastReceiver mNotificationReceiver;
    private BroadcastReceiver mConfigurationReceiver;

    private static final IntentFilter sNotificationIntentFilter = new IntentFilter();
    private static final IntentFilter sConfigurationIntentFilter = new IntentFilter();

    static {
        sConfigurationIntentFilter.addAction(Constants.ACTION_CONFIGURATION_CHANGE);
        sConfigurationIntentFilter.addAction(Constants.ACTION_CHANGE_SCREEN);
        sConfigurationIntentFilter.addAction(Constants.ACTION_INVALIDATE);
        sConfigurationIntentFilter.addCategory(Constants.CATEGORY_GLOBAL);

        sNotificationIntentFilter.addAction(Constants.ACTION_NOTIFY);
        sNotificationIntentFilter.addCategory(Constants.CATEGORY_GLOBAL);
    }

    @Inject
    public MetarhiaBroadcastReceiver(MetarhiaActivity activity, @Named(Constants.GLOBAL_BROADCAST) LocalBroadcastManager broadcastManager) {
        mBroadcastManager = broadcastManager;
        mActivity = activity;

        setUpReceivers();
    }

    private void setUpReceivers() {
        mNotificationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String message = intent.getStringExtra(Constants.DATA_GLOBAL_MESSAGE);
                Toast.makeText(mActivity.getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        };
        mBroadcastManager.registerReceiver(mNotificationReceiver, sNotificationIntentFilter);

        mConfigurationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.ACTION_CHANGE_SCREEN)) {
                    String screenName = intent.getStringExtra(MetarhiaApplication.DATA_SCREEN_NAME);
                    mActivity.changeScreen(screenName);
                } else if (intent.getAction().equals(Constants.ACTION_INVALIDATE)) {
                    mActivity.globalInvalidate();
                } else if (intent.getAction().equals(Constants.ACTION_CONFIGURATION_CHANGE)) {

                }
            }
        };
        mBroadcastManager.registerReceiver(mConfigurationReceiver, sConfigurationIntentFilter);
    }
}
