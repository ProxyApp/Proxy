package com.shareyourproxy.app.fragment;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;

import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.app.BaseActivity;

import io.realm.Realm;
import io.realm.RealmObject;

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

    public RxBusDriver getRxBus() {
        return ((BaseActivity) getActivity()).getRxBus();
    }

    public void transactRealmObject(Realm realm, RealmObject object) {
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(object);
        realm.commitTransaction();
        realm.refresh();
    }

    public void setSupportActionBar(Toolbar toolbar) {
        ((BaseActivity)getActivity()).setSupportActionBar(toolbar);
    }

    public ActionBar getSupportActionBar() {
        return ((BaseActivity)getActivity()).getSupportActionBar();
    }

}
