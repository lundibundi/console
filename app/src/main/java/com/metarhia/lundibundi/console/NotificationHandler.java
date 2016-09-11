package com.metarhia.lundibundi.console;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import com.metarhia.jstp.Connection.JSTPConnection;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.utils.jstphandlers.GlobalNotificationHandler;
import com.metarhia.lundibundi.console.utils.jstphandlers.JSTPGlobalNotificationHandler;

import javax.inject.Inject;
import javax.inject.Named;

import static com.metarhia.lundibundi.console.BroadcastEvents.sendGlobalNotification;

/**
 * Created by lundibundi on 8/30/16.
 */
public class NotificationHandler implements GlobalNotificationHandler {

    private JSTPConnection mConnection;

    private LocalBroadcastManager mLocalBroadcastManager;

    @Inject public NotificationHandler(JSTPConnection connection, @Named(Constants.GLOBAL_BROADCAST) LocalBroadcastManager localBroadcastManager) {
        mLocalBroadcastManager = localBroadcastManager;
        mConnection = connection;

        mConnection.addEventHandler("notification", new JSTPGlobalNotificationHandler(this));
    }

    @Override
    public void onGlobalNotification(String message) {
        sendGlobalNotification(mLocalBroadcastManager, Constants.DATA_GLOBAL_MESSAGE, message);
    }

}
