package com.metarhia.lundibundi.console.dagger;

import android.support.v4.app.Fragment;

import com.metarhia.lundibundi.console.dagger.HasComponent;

/**
 * Created by lundibundi on 7/23/16.
 */
public class BaseFragment extends Fragment {

    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Class<C> componentType) {
        return getComponent(getActivity(), componentType);
    }

    @SuppressWarnings("unchecked")
    protected <C> C getComponent(Object container, Class<C> componentType) {
        return componentType.cast(((HasComponent<C>) container).getComponent());
    }
}
