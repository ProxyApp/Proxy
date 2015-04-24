package com.proxy.app.fragment;

import com.proxy.api.domain.model.User;
import com.proxy.app.BaseActivity;
import com.proxy.event.RxBusDriver;

import io.realm.Realm;

/**
 * Base Fragment abstraction.
 */
public class BaseFragment extends android.support.v4.app.Fragment {

    /**
     * Get the logged in user.
     *
     * @return Logged in user
     */
    public User getLoggedInUser() {
        return ((BaseActivity) getActivity()).getLoggedInUser();
    }

    /**
     * Set the logged in user.
     *
     * @param user update user
     */
    public void setLoggedInUser(User user) {
        ((BaseActivity) getActivity()).setLoggedInUser(user);
    }

    /**
     * Get Default Realm instance.
     *
     * @return logged in user
     */
    public Realm getDefaultRealm() {
        return ((BaseActivity) getActivity()).getDefaultRealm();
    }

    public boolean isLoggedInUser(User user) {
        return user.userId().equals(getLoggedInUser().userId());
    }

    public RxBusDriver getRxBus() {
        return ((BaseActivity) getActivity()).getRxBus();
    }

}
