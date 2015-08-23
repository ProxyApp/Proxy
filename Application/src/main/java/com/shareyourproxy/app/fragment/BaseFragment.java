package com.shareyourproxy.app.fragment;

import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.app.BaseActivity;

/**
 * Base Fragment abstraction.
 */
public class BaseFragment extends Fragment {

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
     */
    public void setLoggedInUser(User user) {
        ((BaseActivity) getActivity()).setLoggedInUser(user);
    }

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public SharedPreferences getSharedPrefrences() {
        return ((BaseActivity) getActivity()).getSharedPreferences();
    }

    /**
     * Get the logged in user.
     *
     * @return Logged in user
     */
    public boolean isLoggedInUser(User user) {
        return ((BaseActivity) getActivity()).isLoggedInUser(user);
    }

    public RxBusDriver getRxBus() {
        return ((BaseActivity) getActivity()).getRxBus();
    }

    public ActionBar getSupportActionBar() {
        return ((BaseActivity) getActivity()).getSupportActionBar();
    }

    public void setSupportActionBar(Toolbar toolbar) {
        ((BaseActivity) getActivity()).setSupportActionBar(toolbar);
    }

}
