package com.shareyourproxy.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.shareyourproxy.Intents;
import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.ShareLinkEvent;
import com.shareyourproxy.api.rx.event.OnBackPressedEvent;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Base abstraction for all activities to inherit from.
 */
public abstract class BaseActivity extends AppCompatActivity {

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
     * Get the common shared preferences used to save a copy of the logged in user.
     *
     * @return common shared preferences
     */
    public SharedPreferences getSharedPreferences() {
        return ((ProxyApplication) getApplication()).getSharedPreferences();
    }

    public boolean isLoggedInUser(@NonNull User user) {
        return getLoggedInUser() != null &&
            user.id().value().equals(getLoggedInUser().id().value());
    }

    /**
     * Get a {@link rx.Observable} to use as an event bus for messages.
     *
     * @return Rx bus observable
     */
    public RxBusDriver getRxBus() {
        return ((ProxyApplication) getApplication()).getRxBus();
    }

    public void buildToolbar(Toolbar toolbar, String title, Drawable icon) {
        setSupportActionBar(toolbar);
        ActionBar bar = getSupportActionBar();
        bar.setTitle(title);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setHomeAsUpIndicator(icon);
    }

    public void buildCustomToolbar(Toolbar toolbar, View customView) {
        toolbar.removeAllViews();
        toolbar.addView(customView);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    }

    /**
     * Function to delete the main realm configuration.
     */
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
        _subscriptions.add(getRxBus().toObserverable()
            .subscribe(onNextEvent()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        getRxBus().post(new OnBackPressedEvent());
    }

    private Action1<Object> onNextEvent() {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof ShareLinkEvent) {
                    launchShareLinkIntent((ShareLinkEvent) event);
                }
            }
        };
    }

    /**
     * Launch an Intent chooser dialog for a Proxy User to select a method of sharing a profile
     * link. The link is an http address to a User's group channels.
     *
     * @param event message data, http link
     */
    public void launchShareLinkIntent(ShareLinkEvent event) {
        Intent sendIntent =
            Intents.getShareLinkIntent(event.message);

        startActivity(
            Intent.createChooser(
                sendIntent, getString(R.string.dialog_sharelink_title)));
    }
}
