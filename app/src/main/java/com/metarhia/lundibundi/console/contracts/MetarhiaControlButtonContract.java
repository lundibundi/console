package com.metarhia.lundibundi.console.contracts;

import android.view.View;

import com.metarhia.lundibundi.console.MetarhiaButton;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.metarhia.lundibundi.console.contracts.MetarhiaContractUtils.*;

/**
 * Created by lundibundi on 8/5/16.
 */
public class MetarhiaControlButtonContract {

    public final static Map<String, ParameterConf> dslToConfiguration = new HashMap<>(6);

    static {
        dslToConfiguration.put("text", new ParameterConf(""));

        registerMethodsOnDSL(dslToConfiguration, MetarhiaControlButtonContract.class);
    }

    private static LinkedList<Class<?>> sortedAppliableContracts = new LinkedList<>();
    static {
        sortedAppliableContracts.add(MetarhiaViewContract.class);
        sortedAppliableContracts.add(MetarhiaControlButtonContract.class);
    }

    public static LinkedList<Class<?>> getSortedAppliableContracts() {
        return sortedAppliableContracts;
    }

    public static boolean updateValue(View view, String name, Object params) {
        return MetarhiaContractUtils.updateValue(dslToConfiguration, view, name, params) || MetarhiaViewContract.updateValue(view, name, params);
    }

    public static void setText(MetarhiaButton metarhiaButton, String text) {
        metarhiaButton.setText(text);
    }
}
