package com.metarhia.lundibundi.console;

import android.content.Context;
import android.util.Log;
import android.view.View;

import android.widget.RelativeLayout;
import com.metarhia.lundibundi.console.contracts.MetarhiaControlEditContract;
import com.metarhia.lundibundi.console.contracts.MetarhiaControlButtonContract;
import com.metarhia.lundibundi.console.contracts.MetarhiaControlLabelContract;
import com.metarhia.lundibundi.console.contracts.MetarhiaScreenContract;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

/**
 * Stores all available views and able to deduce and instantiate them
 */
public class MetarhiaViewFactory extends MetarhiaObjectFactory {

    private static final String LOG_TAG = MetarhiaViewFactory.class.getSimpleName();

    public static final Map<String, ControlConfiguration> metarhiaControls = new HashMap<>(3);

    static {
        metarhiaControls.put(MetarhiaEdit.CONTROL_NAME,
            new ControlConfiguration(MetarhiaEdit.class, MetarhiaControlEditContract.class, R.attr.metarhiaEditStyle));
        metarhiaControls.put(MetarhiaButton.CONTROL_NAME,
            new ControlConfiguration(MetarhiaButton.class, MetarhiaControlButtonContract.class, R.attr.metarhiaButtonStyle));
        metarhiaControls.put(MetarhiaLabel.CONTROL_NAME,
            new ControlConfiguration(MetarhiaLabel.class, MetarhiaControlLabelContract.class, R.attr.metarhiaLabelStyle));
        metarhiaControls.put(MetarhiaScreen.CONTROL_NAME,
            new ControlConfiguration(MetarhiaScreen.class, MetarhiaScreenContract.class, R.attr.metarhiaScreenStyle));

        availableObjects = new HashMap<>();
        for (Map.Entry<String, ControlConfiguration> me : metarhiaControls.entrySet()) {
            availableObjects.put(me.getKey(), me.getValue().clazz);
        }

        typeFieldName = NodeEnv.CONTROL_TYPE_KEY;
    }

    // TODO is is possible to call this method to create MetarhiaScreen which will fail -
    // it must not be allowed to (move MetarhiaScreen from this hierarchy)
    public static MetarhiaControl newInstance(String name, String controlType, Context context, MetarhiaTheme theme) {
        Class c = metarhiaControls.get(controlType).clazz;
        if (c == null) return null;

        try {
            return (MetarhiaControl) c.getConstructor(Context.class, String.class, MetarhiaTheme.class)
                    .newInstance(context, name, theme);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            Log.i(LOG_TAG, "Cannot create view", e);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            Log.i(LOG_TAG, "Cannot create view", e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            Log.i(LOG_TAG, "Cannot create view", e);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            Log.i(LOG_TAG, "Cannot create view", e);
        }

        return null;
    }

    public static Set<String> getAllBasicControls() {
        return metarhiaControls.keySet();
    }

    public static class ViewAttributes {
        public Class clazz;
        public int themeAttributeId;

        public ViewAttributes(Class clazz, int themeAttributeId) {
            this.clazz = clazz;
            this.themeAttributeId = themeAttributeId;
        }
    }

    private MetarhiaActivity mMetarhiaActivity;
    private MetarhiaTheme mMetarhiaTheme;

    @Inject
    public MetarhiaViewFactory(MetarhiaActivity metarhiaActivity,
                               MetarhiaTheme metarhiaTheme) {
        mMetarhiaActivity = metarhiaActivity;
        mMetarhiaTheme = metarhiaTheme;
    }

    public MetarhiaControl get(String name, String controlType) {
        MetarhiaControl mc = newInstance(name, controlType, mMetarhiaActivity, mMetarhiaTheme);
        if (mc == null) return null;

        final View view = (View) mc;
        final int[] lp = mc.getDefaultLayoutParams();
        view.setLayoutParams(new RelativeLayout.LayoutParams(lp[0], lp[1]));
        return mc;
    }

    public MetarhiaControl get(String name, Map<String, ?> configuration) {
        String controlType = (String) configuration.get(NodeEnv.CONTROL_TYPE_KEY);

        return get(name, controlType);
    }

    public static class ControlConfiguration {
        public Class clazz;
        public Class contractClass;
        public Integer styleAttribute;

        public ControlConfiguration(Class clazz, Class contractClass, Integer styleAttribute) {
            this.clazz = clazz;
            this.contractClass = contractClass;
            this.styleAttribute = styleAttribute;
        }
    }
}
