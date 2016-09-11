package com.metarhia.lundibundi.console.utils.jstphandlers;

import com.metarhia.jstp.compiler.annotations.CustomNamed;
import com.metarhia.jstp.compiler.annotations.JSTPReceiver;
import com.metarhia.jstp.compiler.annotations.Typed;
import com.metarhia.jstp.core.JSTypes.JSArray;
import com.metarhia.jstp.core.JSTypes.JSObject;

/**
 * Created by lundibundi on 8/30/16.
 */
@JSTPReceiver
public interface AppConfigurationHandler {
    // assume we get array of format [name(string), screens(object), theme(string, opt)]
    @CustomNamed("config")
    @Typed(JSArray.class)
    void onConfiguration(String appName, JSObject screens, String themeName);
}
