package com.metarhia.lundibundi.console;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;

import android.view.View;
import android.widget.RelativeLayout;
import com.eclipsesource.v8.V8Object;
import com.metarhia.console.compiler.annotations.ApiMethod;
import com.metarhia.lundibundi.console.contracts.MetarhiaViewContract;
import com.metarhia.lundibundi.console.contracts.MetarhiaControlEditContract;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.dagger.HasComponent;
import com.metarhia.lundibundi.console.dagger.components.ActivityComponent;
import com.metarhia.lundibundi.console.utils.Utils;

import java.util.*;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Named;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.metarhia.lundibundi.console.MetarhiaObjectUtils.*;

/**
 * Created by lundibundi on 7/14/16.
 */
@com.metarhia.console.compiler.annotations.MetarhiaObject(
    contracts = {MetarhiaViewContract.class, MetarhiaControlEditContract.class},
    hierarchy = Constants.HIERARCHY_METARHIA_CONTROL)
public class MetarhiaEdit extends TextInputLayout implements MetarhiaControl {

    public static final String CONTROL_NAME = "edit";

    public static final Set<FunctionConf> availableApiMethods = new HashSet<>(3);

    static {
        availableApiMethods.add(new FunctionConf("invalidateControl", "invalidate"));
    }

    private String mName;
    private String mScreenName;

    private List<Runnable> mPostUpdateActions;

    @Inject @Named(Constants.NODE_GLOBAL) NodeEnv mNodeEnv;

    @Inject @Named(Constants.MAIN_HANDLER) Handler mMainHandler;

    private TextInputEditText mEditText;

    private Pattern mCustomFilter;

    private TextWatcher mTextWatcher;

    private MetarhiaObject mParent;

    public MetarhiaEdit(Context context, String name, MetarhiaTheme theme) {
//        super(context, null, theme.getControlStyle(CONTROL_NAME));
        super(context);

        // finish view configuration by adding EditText
        // TODO customize edittext appearance
//        mEditText = new TextInputEditText(context, null, theme.getControlStyle(CONTROL_NAME));
        mEditText = new TextInputEditText(context);
        mEditText.setId(Utils.generateViewId());
        mEditText.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        addView(mEditText);

        setHintEnabled(true);

        mName = name;
        mPostUpdateActions = new LinkedList<Runnable>();

        ((HasComponent<ActivityComponent>) context).getComponent().inject(this);
    }

    @ApiMethod("invalidate")
    @Override
    public void invalidateControl() {
        if (mScreenName == null) return;

        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this,
            mNodeEnv.getControlConfigurationAsync(mScreenName, mName));
    }

    @Override
    public void updateConfiguration(V8Object configuration) {
        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this, configuration);
    }

    @Override
    public void updateConfiguration(Map<String, ?> config) {
        MetarhiaObjectUtils.updateConfiguration(this, config, mPostUpdateActions,
            MetarhiaControlEditContract.class, View.class);

        invalidate();
    }

    @Override
    public void addPostUpdateAction(Runnable runnable) {
        mPostUpdateActions.add(runnable);
    }

    @Override
    public String getJSName() {
        return mName;
    }

    @Override
    public MetarhiaObject getMetarhiaParent() {
        return mParent;
    }

    @Override
    public void setMetarhiaParent(MetarhiaObject parent) {
        mParent = parent;
    }

    @Override
    public void registerControlApi(NodeEnv nodeEnv, String screenName) {
        mScreenName = screenName;
        nodeEnv.registerControlApiAsync(screenName, this, availableApiMethods).start();
    }

    @Override
    public void unregisterControlApi(NodeEnv nodeEnv, String screenName) {
        mScreenName = null;
        mNodeEnv.unregisterControlApiAsync(screenName, mName, availableApiMethods).start();
    }

    @Override
    public int[] getDefaultLayoutParams() {
        return new int[]{MATCH_PARENT, WRAP_CONTENT};
    }

    @Nullable
    @Override
    public TextInputEditText getEditText() {
        return mEditText;
    }

    public void setCustomFilter(String customFilter) {
        mEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        mCustomFilter = Pattern.compile(customFilter);
        mTextWatcher = new TextWatcher() {
            private String backupString;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                backupString = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!mCustomFilter.matcher(s.toString()).matches()) {
                    mEditText.setText(backupString);
                }
            }
        };
        mEditText.addTextChangedListener(mTextWatcher);
    }

    public void removeCustomFilter() {
        mEditText.removeTextChangedListener(mTextWatcher);
        mTextWatcher = null;
    }
}
