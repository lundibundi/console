package com.metarhia.lundibundi.console.utils;

import com.eclipsesource.v8.V8;
import com.metarhia.lundibundi.console.NodeEnv;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by lundibundi on 8/29/16.
 */
public class JSUtils {

    public static final String CREATE_OBJECT_FUNCTION = "createObject";

    public static final Map<String, String> utilFunctions = new HashMap<>();
    static {
        utilFunctions.put("createObject",
               "function(proto, defaults, properties) {\n" +
                       "let obj = Object.create(proto, defaults);\n" +
                       "return Object.assign(obj, properties);\n" +
                       "}");
    }

    public static Map<String, String> addJSUtils(V8 runtime, String prefix) {
        Map<String, String> registeredFunctions = new HashMap<>();
        for (Map.Entry<String, String> me : utilFunctions.entrySet()) {
            final String functionPath = prefix + "." + me.getKey();
            runtime.executeVoidScript(functionPath + " = " + me.getValue());
            registeredFunctions.put(me.getKey(), functionPath);
        }
        return registeredFunctions;
    }

    public static String composeFunctionCall(String name, String... args) {
        StringBuilder builder = new StringBuilder(name);
        builder.append("(");
        int i = 0;
        for (String arg : args) {
            builder.append(arg);
            if (++i < args.length) {
                builder.append(", ");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    // todo make this method directly into j2v8 as c++ method
    public static boolean isUndefined(V8 runtime, String... namespaces) {
        String isUndefined = NodeEnv.composeAccessExpr(namespaces)
            .insert(0, "typeof ")
            .append(" === 'undefined'")
            .toString();
        return runtime.executeBooleanScript(isUndefined);
    }

    public static boolean isAppUndefined(V8 runtime, String... namespaces) {
        String isUndefined = NodeEnv.composeAppAccessExpr(namespaces)
            .insert(0, "typeof ")
            .append(" === 'undefined'")
            .toString();
        return runtime.executeBooleanScript(isUndefined);
    }
}
