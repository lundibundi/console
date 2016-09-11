package com.metarhia.lundibundi.console;

/**
 * Created by lundibundi on 7/23/16.
 */
public interface MetarhiaControl extends MetarhiaObject {
    void invalidateControl();

    void registerControlApi(NodeEnv nodeEnv, String screenName);

    void unregisterControlApi(NodeEnv nodeEnv, String screenName);

    int[] getDefaultLayoutParams();
}
