package com.metarhia.lundibundi.console;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.Log;
import com.metarhia.jstp.compiler.annotations.Indexed;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static com.metarhia.lundibundi.console.MetarhiaViewFactory.*;

/**
 * Created by lundibundi on 7/14/16.
 */
public class MetarhiaTheme {

    private static final String LOG_TAG = MetarhiaTheme.class.getSimpleName();

    private static final String THEME_PREFIX = "Metarhia_AppTheme_";

    /**
     * Name of the theme this class applies
     */
    private String mName;

    /**
     * Global style applied for all views
     * should be defined (defaults to THEME_PREFIX + "Default")
     */
    private int mTheme;


    /**
     * Internal styles for specific "controls"
     * lazily resolved
     */
    private Map<String, Integer> mStyles;

    public MetarhiaTheme(Context context, String name) {
        mStyles = new HashMap<>();
        mTheme = resolveTheme(context, name);
    }

    private int resolveTheme(Context context, String name) {
        mName = THEME_PREFIX + name;
        int themeId = resolveStyleId(mName);

        if (themeId == R.style.Metarhia_AppTheme_Default) {
            mName = THEME_PREFIX + "Default";
        }

        List<String> orderedStyles = new LinkedList<>();
        int[] styleIds = new int[metarhiaControls.size()];
        int i = 0;
        for (Map.Entry<String, ControlConfiguration> me : metarhiaControls.entrySet()) {
            Integer attrId = me.getValue().styleAttribute;
            if (attrId != null) {
                styleIds[i++] = attrId;
                orderedStyles.add(me.getKey());
            }
        }

        TypedArray a = context.getTheme().obtainStyledAttributes(themeId, styleIds);

        i = 0;
        for (String style : orderedStyles) {
            mStyles.put(style, a.getResourceId(i++, R.style.Metarhia_Control_Default));
        }
        a.recycle();

        return themeId;
    }

    private static int resolveStyleId(String name) {
        try {
            Class res = R.style.class;
            Field theme = res.getField(name);
            return theme.getInt(null);
        } catch (NoSuchFieldException e) {
            Log.i(LOG_TAG, "Non existing style requested: " + name);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            Log.i(LOG_TAG, "Non existing style requested: " + name);
            e.printStackTrace();
        }
        return R.style.Metarhia_AppTheme_Default;
    }

    public Integer getControlStyle(String controlName) {
        Integer style = mStyles.get(controlName);
        return style != null ? style : R.style.Metarhia_Control_Default;
    }

    public int getTheme() {
        return mTheme;
    }
}
