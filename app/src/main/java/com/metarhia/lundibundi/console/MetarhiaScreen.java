package com.metarhia.lundibundi.console;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import com.eclipsesource.v8.V8Object;
import com.metarhia.console.compiler.annotations.ApiMethod;
import com.metarhia.lundibundi.console.contracts.MetarhiaScreenContract;
import com.metarhia.lundibundi.console.dagger.BaseFragment;
import com.metarhia.lundibundi.console.dagger.components.ActivityComponent;
import com.metarhia.lundibundi.console.utils.Constants;
import com.metarhia.lundibundi.console.utils.Utils;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.metarhia.lundibundi.console.MetarhiaObjectUtils.FunctionConf;

/**
 * Created by lundibundi on 7/22/16.
 */
@com.metarhia.console.compiler.annotations.MetarhiaObject(
    contracts = {MetarhiaScreenContract.class},
    hierarchy = Constants.HIERARCHY_METARHIA_SCREEN)
public class MetarhiaScreen extends BaseFragment implements
    MetarhiaObject,
    MetarhiaControlGroup {

    public static final String CONTROL_NAME = "screen";

    public static final int styleAttribute = R.attr.metarhiaScreenStyle;

    private static final String SCREEN_NAME_KEY = "screenName";

    private static final String KEY_LAST_CONF_HASH = "LAST_CONF_HASH";

    public static final Class contractClass = MetarhiaScreenContract.class;

    public static final Set<MetarhiaObjectUtils.FunctionConf> availableApiMethods = new HashSet<>();

    static {
        availableApiMethods.add(new FunctionConf("updateConfiguration", "updateConfiguration", V8Object.class));
        availableApiMethods.add(new FunctionConf("invalidate", "invalidate"));
    }

    private String mName;

    private MetarhiaObject mParent;

    @Inject MetarhiaViewFactory mViewFactory;

    @Inject @Named(Constants.NODE_GLOBAL) NodeEnv mNodeEnv;

    @Inject @Named(Constants.MAIN_HANDLER) Handler mMainHandler;

    private RelativeLayout mLayout;

    private Map<String, MetarhiaControl> mControls;

    private boolean mScrollable;

    private Integer mLastConfHash;

    private List<Runnable> mPostUpdateActions;

    public static MetarhiaScreen newInstance(String name) {
        MetarhiaScreen metarhiaScreen = new MetarhiaScreen();

        Bundle args = new Bundle();
        args.putString(SCREEN_NAME_KEY, name);
        metarhiaScreen.setArguments(args);

        return metarhiaScreen;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ScrollView parent = (ScrollView) inflater.inflate(R.layout.fragment_screen, container, false);

        // allow to disable scrolling
        parent.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return !mScrollable;
            }
        });

        mLayout = (RelativeLayout) parent.findViewById(R.id.screen_layout);

        return parent;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getComponent(getActivity(), ActivityComponent.class).inject(this);

        mName = getArguments().getString(SCREEN_NAME_KEY);
        mControls = new HashMap<>();
        mPostUpdateActions = new LinkedList<>();

        if (savedInstanceState != null) {
            mLastConfHash = savedInstanceState.getInt(KEY_LAST_CONF_HASH);
        }
    }

    public void checkUpdateConfiguration() {
        mNodeEnv.getScreenConfigurationAsync(mName)
            .thenApply(new Utils.Function<V8Object, Void>() {
                @Override
                public Void apply(V8Object config) throws Exception {
                    int hashCode = config.hashCode();
                    if (mLastConfHash == null
                        || hashCode != mLastConfHash) {
                        mLastConfHash = hashCode;
                        updateConfiguration(config);
                    }
                    return null;
                }
            }).start();
    }

    @Override
    public void onResume() {
        super.onResume();

        // todo save controls between recreation and register them again, consider splitting MetarhiaControl into control and view
        mNodeEnv.registerScreenApiAsync(this, availableApiMethods).start();
        checkUpdateConfiguration();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putInt(KEY_LAST_CONF_HASH, mLastConfHash);
    }

    @Override
    public void onStop() {
        super.onStop();
        for (MetarhiaControl mc : mControls.values()) {
            mc.unregisterControlApi(mNodeEnv, mName);
        }
        mNodeEnv.unregisterScreenApiAsync(mName, availableApiMethods).start();
    }

    public void addControls(Map<String, ?> controls) {
        for (Map.Entry<String, ?> me : controls.entrySet()) {
            addControl(me.getKey(), (Map<String, ?>) me.getValue());
        }
        mLayout.invalidate();
    }

    public void addControl(String name, Map<String, ?> configuration) {
        final MetarhiaControl control = mControls.get(name);
        if (control != null) {
            control.updateConfiguration(configuration);
            return;
        }

        MetarhiaControl mc = mViewFactory.get(name, configuration);
        if (mc == null) return;
        View view = (View) mc;

        int id = Utils.generateViewId();
        view.setId(id);

        mc.setMetarhiaParent(this);
        mc.updateConfiguration(configuration);
        mc.registerControlApi(mNodeEnv, mName);

        mLayout.addView(view);
        mControls.put(name, mc);

        // todo should we remove api when view is detached?
//        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
//            @Override
//            public void onViewAttachedToWindow(View v) {
//                ((MetarhiaControl) v).registerControlApi(mNodeEnv);
//            }
//
//            @Override
//            public void onViewDetachedFromWindow(View v) {
//                ((MetarhiaControl) v).unregisterControlApi(mNodeEnv);
//            }
//        });
    }

    @ApiMethod
    public void invalidate() {
        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this,
            mNodeEnv.getScreenConfigurationAsync(mName));
    }

    @Override
    public void updateConfiguration(V8Object configuration) {
        MetarhiaObjectUtils.postUpdateConfiguration(mNodeEnv, mMainHandler, this, configuration);
    }

    @Override
    public void updateConfiguration(Map<String, ?> config) {
        MetarhiaObjectUtils.updateConfiguration(this, config, mPostUpdateActions,
            MetarhiaScreenContract.class, MetarhiaScreen.class);

        mLayout.invalidate();
    }

    @Override
    public void addPostUpdateAction(Runnable action) {
        mPostUpdateActions.add(action);
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
    public Map<String, MetarhiaControl> getChildControls() {
        return mControls;
    }

    public ViewGroup getLayout() {
        return mLayout;
    }

    public boolean isScrollable() {
        return mScrollable;
    }

    public void setScrollable(boolean scrollable) {
        this.mScrollable = scrollable;
    }

    public int getControlId(String controlName) {
        final View view = (View) mControls.get(controlName);
        return view == null ? -1 : view.getId();
    }

//    FrameLayout.LayoutParams lp  =new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
//    TextInputLayout titleWrapper = new TextInputLayout(getActivity());
//titleWrapper.setLayoutParams(lp);
//    EditText et = new TextInputEditText(getActivity());
//    et.setId(Utils.generateViewId());
//        et.setLayoutParams(lp);
//        et.setPadding(5, 5, 5, 5);
//    titleWrapper.addView(et);
//titleWrapper.setHint("shiiiiiiiiiiiiiiiiiiiiiiiit");
//    MetarhiaViewUtils.setMargins(titleWrapper, 100, 0, 0, 50);
//mLayout.addView(titleWrapper);

//    MetarhiaButton b = new MetarhiaButton("shit", null, getActivity(), mMetarhiaTheme);
//b.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//MetarhiaViewContract.setMargins(b, 100, 0, 0, 100);
//b.setText("loooooooooooooooooooooooooooooooooooooooooooooooooooooong");
//mLayout.addView(b);

//    MetarhiaLabel l = new MetarhiaLabel("sshit", null, getActivity(), mMetarhiaTheme);
//l.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT));
//MetarhiaViewContract.setMargins(l, 200, 0, 0, 100);
//l.setText("loooooooooooooooooooooooooooooooooooooooooooooooooooooong");
//mLayout.addView(l);

//    MetarhiaLabel l = new MetarhiaLabel(getActivity(), "shit", null);
//l.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
//MetarhiaViewUtils.setMargins(l, 200, 0, 0, 100);
//l.setText("loooooooooooooooooooooooooooooooooooooooooooooooooooooong");
//    MetarhiaViewContract.setAlignment(l, "bottom");
//RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        layoutParams.addRule(alignmentDsl.get(alignment));
//        view.setLayoutParams(layoutParams);
//mLayout.addView(l);
}
