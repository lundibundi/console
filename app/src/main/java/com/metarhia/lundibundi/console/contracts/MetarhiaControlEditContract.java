package com.metarhia.lundibundi.console.contracts;

import android.support.design.widget.TextInputEditText;
import android.view.View;

import android.view.ViewGroup;
import com.metarhia.lundibundi.console.MetarhiaEdit;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.metarhia.lundibundi.console.contracts.MetarhiaContractUtils.*;

/**
 * Created by lundibundi on 8/5/16.
 */
public class MetarhiaControlEditContract {

    public final static Map<String, ParameterConf> dslToConfiguration = new HashMap<>(6);

    static {
        dslToConfiguration.put("filter", new ParameterConf("none"));
        dslToConfiguration.put("label", new ParameterConf(""));

        dslToConfiguration.put("width", new ParameterConf(String.valueOf(ViewGroup.LayoutParams.MATCH_PARENT)));

        registerMethodsOnDSL(dslToConfiguration, MetarhiaViewContract.class);
    }

    // contains either Integer of InputType or custom String regex
    private final static Map<String, Object> filterDsl = new HashMap<>(6);

    static {
        filterDsl.put("login", "\\w+");
    }

    private static LinkedList<Class<?>> sortedAppliableContracts = new LinkedList<>();

    static {
        sortedAppliableContracts.add(MetarhiaViewContract.class);
        sortedAppliableContracts.add(MetarhiaControlEditContract.class);
    }

    public static LinkedList<Class<?>> getSortedAppliableContracts() {
        return sortedAppliableContracts;
    }

    public static boolean updateValue(View view, String name, Object params) {
        return MetarhiaContractUtils.updateValue(dslToConfiguration, view, name, params) || MetarhiaViewContract.updateValue(view, name, params);
    }

    public static void setFilter(MetarhiaEdit metarhiaEdit, Object conf) {
        Object inputType = filterDsl.get(conf);
        if (inputType instanceof Integer) {
            TextInputEditText editText = metarhiaEdit.getEditText();
            if (editText != null) editText.setInputType((Integer) inputType);
        } else {
            String it = (String) inputType;
            metarhiaEdit.setCustomFilter(it);
        }
    }

    public static void setLabel(MetarhiaEdit metarhiaEdit, String text) {
        metarhiaEdit.setHint(text);
    }

    public static void setWidth(View view, int width) {
        MetarhiaViewContract.setWidth(view, width);
    }
}
