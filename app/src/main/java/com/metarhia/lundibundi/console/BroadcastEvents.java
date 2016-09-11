package com.metarhia.lundibundi.console;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.metarhia.lundibundi.console.utils.Constants;

/**
 * Created by lundibundi on 8/30/16.
 */
public class BroadcastEvents {

    public static void sendInvalidationEvent(LocalBroadcastManager broadcastManager) {
        Intent invalidationIntent = new Intent(Constants.ACTION_INVALIDATE);
        invalidationIntent.addCategory(Constants.CATEGORY_GLOBAL);
        broadcastManager.sendBroadcast(invalidationIntent);
    }

    public static void changeScreenEvent(LocalBroadcastManager broadcastManager, String keyScreenName, String screenName) {
        Intent changeScreenIntent = new Intent(Constants.ACTION_CHANGE_SCREEN);
        changeScreenIntent.addCategory(Constants.CATEGORY_GLOBAL);
        changeScreenIntent.putExtra(keyScreenName, screenName);
        broadcastManager.sendBroadcast(changeScreenIntent);
    }

    public static void sendGlobalNotification(LocalBroadcastManager broadcastManager, String keyMessage, String message) {
        Intent notificationIntent = new Intent(Constants.ACTION_NOTIFY);
        notificationIntent.addCategory(Constants.CATEGORY_GLOBAL);
        notificationIntent.putExtra(keyMessage, message);
        broadcastManager.sendBroadcast(notificationIntent);
    }
}
