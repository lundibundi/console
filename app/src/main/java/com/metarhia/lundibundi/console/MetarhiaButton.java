package com.metarhia.lundibundi.console;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import com.eclipsesource.v8.V8Object;
import com.metarhia.console.compiler.annotations.ApiMethod;
import com.metarhia.lundibundi.console.contracts.MetarhiaControlButtonContract;
import com.metarhia.lundibundi.console.contracts.MetarhiaViewContract;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.dagger.HasComponent;
import com.metarhia.lundibundi.console.dagger.components.ActivityComponent;

import java.util.*;

import javax.inject.Inject;
import javax.inject.Named;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static com.metarhia.lundibundi.console.MetarhiaObjectUtils.*;

/**
 * Created by lundibundi on 7/23/16.
 */
@com.metarhia.console.compiler.annotations.MetarhiaObject(
    contracts = {MetarhiaViewContract.class, MetarhiaControlButtonContract.class},
    hierarchy = Constants.HIERARCHY_METARHIA_CONTROL)
public class MetarhiaButton extends Button implements MetarhiaControl {

    public static final String CONTROL_NAME = "button";

    public static final Set<FunctionConf> availableApiMethods = new HashSet<>(3);

    static {
        availableApiMethods.add(new FunctionConf("invalidateControl", "invalidate"));
    }

    private String mName;
    private String mScreenName;

    @Inject @Named(Constants.NODE_GLOBAL) NodeEnv mNodeEnv;

    @Inject @Named(Constants.MAIN_HANDLER) Handler mMainHandler;

    private MetarhiaObject mParent;

    private List<Runnable> mPostUpdateActions;

    public MetarhiaButton(Context context, String name, MetarhiaTheme theme) {
//        super(context, null, theme.getControlStyle(CONTROL_NAME));
        super(context);

        mName = name;
        mPostUpdateActions = new LinkedList<Runnable>();

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
            MetarhiaControlButtonContract.class, View.class);

        invalidate();
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
    public int[] getDefaultLayoutParams() {
        return new int[]{WRAP_CONTENT, WRAP_CONTENT};
    }

    @Override
    public void addPostUpdateAction(Runnable runnable) {
        mPostUpdateActions.add(runnable);
    }

    public String getJSName() {
        return mName;
    }
}
