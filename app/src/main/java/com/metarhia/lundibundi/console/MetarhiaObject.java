package com.metarhia.lundibundi.console;

import com.eclipsesource.v8.V8Object;

import java.util.Map;
import java.util.Set;

/**
 * Created by lundibundi on 7/25/16.
 */
public interface MetarhiaObject {
    void updateConfiguration(Map<String, ? extends Object> configuration);

    void updateConfiguration(final V8Object configuration);

    void addPostUpdateAction(Runnable action);

    String getJSName();

    MetarhiaObject getMetarhiaParent();

    void setMetarhiaParent(MetarhiaObject parent);

//    public static final Class contractClass;

//    public static final Set<FunctionConf> availableApiMethods;
}
