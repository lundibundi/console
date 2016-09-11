package com.metarhia.lundibundi.console.contracts;

import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.metarhia.lundibundi.console.MetarhiaControl;
import com.metarhia.lundibundi.console.MetarhiaScreen;
import com.metarhia.lundibundi.console.MetarhiaViewUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.metarhia.lundibundi.console.contracts.MetarhiaContractUtils.*;

/**
 * Created by lundibundi on 7/25/16.
 */
public class MetarhiaViewContract {

    public final static Map<String, ParameterConf> dslToConfiguration = new HashMap<>(6);

    private static final Map<String, Integer> relativenessDsl = new HashMap<>(5);

    private static final Map<String, Integer> alignmentDsl = new HashMap<>(5);

    static {
        dslToConfiguration.put("visible", new ParameterConf("true"));
        dslToConfiguration.put("enabled", new ParameterConf("true"));
        dslToConfiguration.put("top", new ParameterConf("0"));
        dslToConfiguration.put("right", new ParameterConf("0"));
        dslToConfiguration.put("bottom", new ParameterConf("0"));
        dslToConfiguration.put("left", new ParameterConf("0"));
        dslToConfiguration.put("align", new ParameterConf("setAlignment", "0"));
        dslToConfiguration.put("height", new ParameterConf(String.valueOf(ViewGroup.LayoutParams.WRAP_CONTENT)));
        dslToConfiguration.put("width", new ParameterConf(String.valueOf(ViewGroup.LayoutParams.WRAP_CONTENT)));

        alignmentDsl.put("client", RelativeLayout.CENTER_IN_PARENT);
        alignmentDsl.put("centerH", RelativeLayout.CENTER_HORIZONTAL);
        alignmentDsl.put("centerV", RelativeLayout.CENTER_VERTICAL);
        alignmentDsl.put("top", RelativeLayout.ALIGN_PARENT_TOP);
        alignmentDsl.put("right", RelativeLayout.ALIGN_PARENT_RIGHT);
        alignmentDsl.put("bottom", RelativeLayout.ALIGN_PARENT_BOTTOM);
        alignmentDsl.put("left", RelativeLayout.ALIGN_PARENT_LEFT);

        relativenessDsl.put("below", RelativeLayout.BELOW);
        relativenessDsl.put("rightOf", RelativeLayout.RIGHT_OF);
        relativenessDsl.put("leftOf", RelativeLayout.LEFT_OF);
        relativenessDsl.put("above", RelativeLayout.ABOVE);

        for (String key : relativenessDsl.keySet()) {
            dslToConfiguration.put(key, new ParameterConf("addRelativeRule", "", null, true, true));
        }

        registerMethodsOnDSL(dslToConfiguration, MetarhiaViewContract.class);
        registerCustomMethodsOnDSL(dslToConfiguration, MetarhiaViewContract.class);
    }

    private static LinkedList<Class<?>> sortedAppliableContracts = new LinkedList<>();

    static {
        sortedAppliableContracts.add(MetarhiaViewContract.class);
    }

    public static LinkedList<Class<?>> getSortedAppliableContracts() {
        return sortedAppliableContracts;
    }

    public static boolean updateValue(View view, String name, Object params) {
        return MetarhiaContractUtils.updateValue(dslToConfiguration, view, name, params);
    }

    public static void setVisibility(View view, boolean v) {
        int visibility = v ? View.VISIBLE : View.INVISIBLE;
        view.setVisibility(visibility);
    }

    public static void setEnabled(View view, boolean enabled) {
        view.setEnabled(enabled);
    }

    public static void setTop(View view, int top) {
        MetarhiaViewUtils.setMargins(view, -1, top, -1, -1);
    }

    public static void setRight(View view, int right) {
        MetarhiaViewUtils.setMargins(view, -1, -1, right, -1);
    }

    public static void setBottom(View view, int bottom) {
        MetarhiaViewUtils.setMargins(view, -1, -1, -1, bottom);
    }

    public static void setLeft(View view, int left) {
        MetarhiaViewUtils.setMargins(view, left, -1, -1, -1);
    }

    public static void setAlignment(View view, String alignment) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
        layoutParams.addRule(alignmentDsl.get(alignment));
        view.setLayoutParams(layoutParams);
    }

    // todo check the need to use correct layout type annd not just ViewGroups's one
    public static void setHeight(View view, int height) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        params.height = height;
        view.setLayoutParams(params);
    }

    public static void setWidth(View view, int width) {
        final ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        view.setLayoutParams(params);
    }

    public static void addRelativeRule(final View view, final String key, final String controlName) {
        MetarhiaControl mc = (MetarhiaControl) view;
        if (!addRelativeRuleImpl(view, key, controlName)) {
            // post to parent as we need to apply them when all controls are available
            mc.getMetarhiaParent().addPostUpdateAction(new Runnable() {
                @Override
                public void run() {
                    addRelativeRuleImpl(view, controlName, key);
                }
            });
        }
    }

    private static boolean addRelativeRuleImpl(View view, String controlName, String key) {
        MetarhiaControl mc = (MetarhiaControl) view;
        int controlId = ((MetarhiaScreen) mc.getMetarhiaParent()).getControlId(controlName);
        if (controlId == -1) return false;

        int ruleId = relativenessDsl.get(key);
        final RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) view.getLayoutParams();
        lp.addRule(ruleId, controlId);
        return true;
    }

}
