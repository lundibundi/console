package com.metarhia.lundibundi.console.utils.jstphandlers;

import com.metarhia.jstp.compiler.annotations.JSTPReceiver;
import com.metarhia.jstp.compiler.annotations.Named;

/**
 * Created by lundibundi on 8/30/16.
 */
@JSTPReceiver
public interface GlobalNotificationHandler {
    @Named("message")
    void onGlobalNotification(String message);
}
