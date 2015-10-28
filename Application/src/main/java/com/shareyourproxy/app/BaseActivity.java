package com.shareyourproxy.app;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.api.domain.factory.AutoValueClass;
import com.shareyourproxy.api.domain.factory.AutoValueTypeAdapterFactory;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.OnBackPressedEvent;
import com.shareyourproxy.api.rx.event.ShareLinkEvent;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.IntentLauncher.launchShareLinkIntent;

/**
 * Base abstraction for all activities to inherit from.
 */
public abstract class BaseActivity extends AppCompatActivity {

    public static final String SCOPE_EMAIL =
        "https://www.googleapis.com/auth/plus.profile.emails.read";
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
     * This prevents the Android status bar and navigation bar from flashing during a transition
     * animation bundled in {@link IntentLauncher#launchSearchActivity(Activity, View, View, View)}
     * and {@link IntentLauncher#launchUserProfileActivity(Activity, User, String, View, View)}.
     */
    public void preventStatusBarFlash(final Activity activity) {
        ActivityCompat.postponeEnterTransition(activity);
        final View decor = activity.getWindow().getDecorView();
        decor.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                decor.getViewTreeObserver().removeOnPreDrawListener(this);
                ActivityCompat.startPostponedEnterTransition(activity);
                return true;
            }
        });
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
            user.id().equals(getLoggedInUser().id());
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
        RealmConfiguration config = new RealmConfiguration.Builder(this)
            .deleteRealmIfMigrationNeeded()
            .schemaVersion(BuildConfig.VERSION_CODE)
            .build();
        Realm.deleteRealm(config);
    }

    public User getSharedPrefJsonUser() {
        User user = null;
        String jsonUser = getSharedPreferences()
            .getString(Constants.KEY_LOGGED_IN_USER, null);
        Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueTypeAdapterFactory())
            .create();
        try {
            user = (User) gson.fromJson(
                jsonUser, User.class.getAnnotation(AutoValueClass.class).autoValueClass());
        } catch (Exception e) {
            Timber.e(Log.getStackTraceString(e));
        }
        return user;
    }

    @Override
    protected void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent(this)));
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

    private Action1<Object> onNextEvent(final Activity activity) {
        return new Action1<Object>() {
            @Override
            public void call(Object event) {
                if (event instanceof ShareLinkEvent) {
                    launchShareLinkIntent(activity, (ShareLinkEvent) event);
                }
            }
        };
    }


}
