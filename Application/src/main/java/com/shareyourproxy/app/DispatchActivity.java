package com.shareyourproxy.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.firebase.client.AuthData;
import com.google.android.gms.common.ConnectionResult;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.command.SyncContactsCommand;
import com.shareyourproxy.api.rx.event.SyncAllContactsErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllContactsSuccessEvent;
import com.shareyourproxy.app.fragment.DispatchFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION;
import static com.shareyourproxy.IntentLauncher.launchLoginActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.api.rx.RxHelper.checkCompositeButton;
import static com.shareyourproxy.api.rx.RxHelper.updateRealmUser;

/**
 * Activity to check if we have a cached user in SharedPreferences. Send the user to the {@link
 * MainActivity} if we have a cached user or send them to {@link LoginActivity} if we need to login
 * and download a current user. Delete cached Realm data on startup.
 */
public class DispatchActivity extends GoogleApiActivity {
    private CompositeSubscription _subscriptions = new CompositeSubscription();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispatch);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_dispatch_container,
                    DispatchFragment.newInstance()).commit();
        }
        initialize();
    }

    /**
     * Initialize a reference to the Applications GoogleApiClient before anything else in this
     * activity.
     */
    private void initialize() {
        deleteRealm();
        goFullScreen();
    }

    private void goFullScreen() {
        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
    }

    @Override
    public void onAuthenticated(AuthData authData) {

        _subscriptions.add(loginObservable(this).subscribe());
    }

    @Override
    public void onAuthenticationError(Throwable e) {
        showErrorDialog(this, e.getMessage());
    }

    @Override
    public void onConnected(Bundle bundle) {
        loginToFirebase(this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        launchLoginActivity(this);
        finish();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        launchLoginActivity(this);
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = checkCompositeButton(_subscriptions);
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(getRxBusObserver()));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    public JustObserver<Object> getRxBusObserver() {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof SyncAllContactsSuccessEvent) {
                    login();
                } else if (event instanceof SyncAllContactsErrorEvent) {
                    login();
                }
            }
        };
    }

    private void login() {
        launchMainActivity(this, MainFragment.ARG_SELECT_PROFILE_TAB, false, null);
        finish();
    }

    private Observable<User> loginObservable(final Activity activity) {
        return Observable.create(
            new Observable.OnSubscribe<User>() {
                @Override
                public void call(final Subscriber<? super User> subscriber) {
                    try {
                        //even if we have a user saved, if this isnt
                        // present, go to login.
                        if (getSharedPreferences().getBoolean
                            (KEY_PLAY_INTRODUCTION, false)) {
                            launchLoginActivity(activity);
                            finish();
                        } else {
                            //get the shared preferences user
                            User user = null;
                            String jsonUser = getSharedPreferences()
                                .getString(Constants
                                    .KEY_LOGGED_IN_USER, null);
                            if (jsonUser != null) {
                                try {
                                    user = getSharedPrefJsonUser();
                                } catch (Exception e) {
                                    Timber.e(Log.getStackTraceString(e));
                                }
                            }
                            // if there is a user saved in shared prefs
                            if (user != null) {
                                RestClient.getUserService(activity)
                                    .updateUserVersion(user.id(),
                                        BuildConfig.VERSION_CODE)
                                    .toBlocking().single();
                                setLoggedInUser(user);
                                updateRealmUser(activity, user);
                                getRxBus().post(new SyncContactsCommand(user));
                                subscriber.onNext(user);
                            } else {
                                launchLoginActivity(activity);
                                finish();
                            }
                        }
                    } catch (Exception e) {
                        subscriber.onError(e);
                    } finally {
                        subscriber.onCompleted();
                    }
                }
            }
        ).compose(RxHelper.<User>subThreadObserveMain());
    }
}
