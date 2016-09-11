package com.metarhia.lundibundi.console.contracts;

import com.metarhia.lundibundi.console.MetarhiaScreen;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.metarhia.lundibundi.console.contracts.MetarhiaContractUtils.*;

/**
 * Created by lundibundi on 7/25/16.
 */
public class MetarhiaScreenContract {

    public final static Map<String, ParameterConf> dslToConfiguration = new HashMap<>(6);

    static {
        dslToConfiguration.put("scrollable", new ParameterConf("none"));
        dslToConfiguration.put("controls", new ParameterConf("{}"));

        registerMethodsOnDSL(dslToConfiguration, MetarhiaScreenContract.class);
    }

    private static LinkedList<Class<?>> sortedAppliableContracts = new LinkedList<>();
    static {
        sortedAppliableContracts.add(MetarhiaViewContract.class);
        sortedAppliableContracts.add(MetarhiaScreenContract.class);
    }

    public static LinkedList<Class<?>> getSortedAppliableContracts() {
        return sortedAppliableContracts;
    }

    public static boolean updateValue(MetarhiaScreen metarhiaScreen, String name, Object params) {
        return MetarhiaContractUtils.updateValue(dslToConfiguration, metarhiaScreen, name, params)
            || MetarhiaViewContract.updateValue(metarhiaScreen.getLayout(), name, params);
    }

    public static void addControls(MetarhiaScreen screen, Map<String, ?> controlConfigurations) {
        screen.addControls(controlConfigurations);
    }

    public static void setScrollable(MetarhiaScreen screen, Boolean scrollable) {
        screen.setScrollable(scrollable);
    }


}
