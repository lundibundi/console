package com.metarhia.lundibundi.console;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.TextView;

import com.eclipsesource.v8.V8Object;
import com.metarhia.console.compiler.annotations.ApiMethod;
import com.metarhia.lundibundi.console.contracts.MetarhiaViewContract;
import com.metarhia.lundibundi.console.contracts.MetarhiaControlLabelContract;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.dagger.HasComponent;
import com.metarhia.lundibundi.console.dagger.components.ActivityComponent;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

@com.metarhia.console.compiler.annotations.MetarhiaObject(
    contracts = {MetarhiaViewContract.class, MetarhiaControlLabelContract.class},
    hierarchy = Constants.HIERARCHY_METARHIA_CONTROL)
public class MetarhiaLabel extends TextView implements MetarhiaControl {

    public static final String CONTROL_NAME = "label";

    public static final Set<MetarhiaObjectUtils.FunctionConf> availableApiMethods = new HashSet<>(3);

    static {
        availableApiMethods.add(new MetarhiaObjectUtils.FunctionConf("invalidateControl", "invalidate"));
    }

    private String mName;

    private String mScreenName;

    @Inject @Named(Constants.NODE_GLOBAL) NodeEnv mNodeEnv;

    @Inject @Named(Constants.MAIN_HANDLER) Handler mMainHandler;

    private List<Runnable> mPostUpdateActions;

    private MetarhiaObject mParent;

    public MetarhiaLabel(Context context, String name, MetarhiaTheme theme) {
//        super(context, null, theme.getControlStyle(CONTROL_NAME));
        super(context);

        mName = name;
        mPostUpdateActions = new LinkedList<>();

        ((HasComponent<ActivityComponent>) context).getComponent().inject(this);
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
            MetarhiaControlLabelContract.class, View.class);

        invalidate();
    }

    @Override
    public int[] getDefaultLayoutParams() {
        return new int[]{WRAP_CONTENT, WRAP_CONTENT};
    }

    @Override
    public void addPostUpdateAction(Runnable runnable) {
        mPostUpdateActions.add(runnable);
    }

    @Override
    public MetarhiaObject getMetarhiaParent() {
        return mParent;
    }

    @Override
    public void setMetarhiaParent(MetarhiaObject parent) {
        mParent = parent;
    }

    public String getJSName() {
        return mName;
    }
}

