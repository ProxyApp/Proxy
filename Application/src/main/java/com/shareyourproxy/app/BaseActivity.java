package com.shareyourproxy.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.shareyourproxy.Intents;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.ProxyApplication;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.ShareLinkEvent;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

import static rx.android.app.AppObservable.bindActivity;

/**
 * Base abstraction for all Activities to inherit from.
 */
public class BaseActivity extends AppCompatActivity {

    private CompositeSubscription _subscriptions;

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public User getLoggedInUser() {
        return ((ProxyApplication) getApplication()).getCurrentUser();
    }

    /**
     * Set the currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @param user currently logged in
     */
    public void setLoggedInUser(User user) {
        ((ProxyApplication) getApplication()).setCurrentUser(user);
    }

    /**
     * Get currently logged in {@link User} in this {@link ProxyApplication}.
     *
     * @return logged in user
     */
    public SharedPreferences getSharedPreferences() {
        return ((ProxyApplication) getApplication()).getSharedPreferences();
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

    @Override
    protected void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindActivity(this, getRxBus().toObserverable())
            .subscribe(onNextEvent()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof ShareLinkEvent) {
                    Intent sendIntent =
                        Intents.getShareLinkIntent(((ShareLinkEvent) event).message);

                    startActivity(
                        Intent.createChooser(
                            sendIntent, getString(R.string.dialog_sharelink_title)));
                }
            }
        };
    }
}
