package com.proxy.app.fragment;

import com.proxy.api.domain.model.User;
import com.proxy.api.rx.RxBusDriver;
import com.proxy.app.BaseActivity;

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

}
