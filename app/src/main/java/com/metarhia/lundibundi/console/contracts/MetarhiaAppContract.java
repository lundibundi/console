package com.metarhia.lundibundi.console.contracts;

import com.metarhia.lundibundi.console.MetarhiaApplication;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.metarhia.lundibundi.console.contracts.MetarhiaContractUtils.*;

/**
 * Created by lundibundi on 8/30/16.
 */
public class MetarhiaAppContract {

    public final static Map<String, ParameterConf> dslToConfiguration = new HashMap<>(6);

    static {
        dslToConfiguration.put("name", new ParameterConf(""));

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

    public static boolean updateValue(MetarhiaApplication application, String name, Object params) {
        return MetarhiaContractUtils.updateValue(dslToConfiguration, application, name, params);
    }

    public static void setName(MetarhiaApplication application, String name) {
        application.setName(name);
    }

}
