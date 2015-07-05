package com.shareyourproxy.app;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.shareyourproxy.api.domain.model.ProxyApplication;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Base abstraction for all Activities to inherit from.
 */
public class BaseActivity extends AppCompatActivity {

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public User getLoggedInUser() {
        return ((ProxyApplication) getApplication()).getCurrentUser();
    }

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public SharedPreferences getSharedPreferences() {
        return ((ProxyApplication) getApplication()).getSharedPreferences();
    }

    /**
     * Set the currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @param user currently logged in
     */
    public void setLoggedInUser(User user) {
        ((ProxyApplication) getApplication()).setCurrentUser(user);
    }

    public boolean isLoggedInUser(@NonNull User user) {
        return getLoggedInUser() != null &&
            user.id().value().equals(getLoggedInUser().id().value());
    }

    public RxBusDriver getRxBus() {
        return ((ProxyApplication) getApplication()).getRxBus();
    }

    protected void buildToolbar(Toolbar toolbar, String title, Drawable icon) {
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(title);
        bar.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(icon);
    }

    protected void deleteRealm() {
        Realm realm = Realm.getDefaultInstance();
        RealmConfiguration config = realm.getConfiguration();
        realm.close();
        Realm.deleteRealm(config);
    }

}
