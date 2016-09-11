package com.metarhia.lundibundi.console;

import android.view.View;

import android.view.ViewGroup;
import com.metarhia.lundibundi.console.contracts.MetarhiaViewContract;

import java.util.List;
import java.util.Map;

import static com.metarhia.lundibundi.console.MetarhiaViewFactory.*;

/**
 * Created by lundibundi on 8/1/16.
 */
public class MetarhiaViewUtils {

    private MetarhiaViewUtils() { }

    public static void updateConfiguration(View view, Map<String, ?> configuration) {
        for (Map.Entry<String, ?> me : configuration.entrySet()) {
            MetarhiaViewContract.updateValue(view, me.getKey(), me.getValue());
        }
    }


//    public static Integer getControlStyleAttr(Class<? extends MetarhiaControl> clazz) {
//        try {
//            return (int) clazz.getDeclaredField("styleAttribute").get(null);
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (NoSuchFieldException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    public static String getControlProto(String controlType) {
        final ControlConfiguration cc = metarhiaControls.get(controlType);
        if (cc != null) {
            return MetarhiaObjectUtils.getObjectProto(cc.clazz);
        }
        return null;
    }

    public static String getControlDefaults(String controlType) {
        final ControlConfiguration cc = metarhiaControls.get(controlType);
        if (cc != null) {
            return MetarhiaObjectUtils.getObjectDefaults(cc.contractClass);
        }
        return null;
    }

    public static void processPostUpdate(List<Runnable> postUpdateActions) {
        for (Runnable action : postUpdateActions) {
            action.run();
        }
    }

    public static void setMargins(View view, int left, int top, int right, int bottom) {
        final ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        if (top >= 0) lp.topMargin = top;
        if (right >= 0) lp.rightMargin = right;
        if (bottom >= 0) lp.bottomMargin = bottom;
        if (left >= 0) lp.leftMargin = left;
        view.setLayoutParams(lp);
    }
}
