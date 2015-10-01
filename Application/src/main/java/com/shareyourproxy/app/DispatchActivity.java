package com.shareyourproxy.app;

import android.app.Activity;
import android.os.Bundle;

import com.firebase.client.AuthData;
import com.google.android.gms.common.ConnectionResult;
import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.gson.UserTypeAdapter;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.event.SyncAllUsersErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.app.fragment.DispatchFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import rx.Observable;
import rx.Subscriber;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.IntentLauncher.launchLoginActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;

/**
 * Activity to check if we have a cached user in SharedPreferences. Send the user to the {@link
 * MainActivity} if we have a cached user or send them to {@link LoginActivity} if we need to login
 * and download a current user. Delete cached Realm data on startup.
 */
public class DispatchActivity extends GoogleApiActivity {
    private CompositeSubscription _subscriptions;

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
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        launchLoginActivity(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
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
            public void success(Object event) {
                if (event instanceof SyncAllUsersSuccessEvent) {
                    login();
                } else if (event instanceof SyncAllUsersErrorEvent) {
                    login();
                }
            }

            @Override
            public void error(Throwable e) {
                Timber.e(e.getMessage());
            }
        };
    }

    private void login() {
        launchMainActivity(this, MainFragment.ARG_SELECT_CONTACTS_TAB, false, null);
        finish();
    }

    private Observable<User> loginObservable(final Activity activity) {
        return Observable.create(new Observable.OnSubscribe<User>() {
            @Override
            public void call(final Subscriber<? super User> subscriber) {
                try {
                    //even if we have a user saved, if this isnt present, go to login.
                    if (!getSharedPreferences().contains(Constants.KEY_PLAYED_INTRODUCTION)) {
                        launchLoginActivity(activity);
                    } else {
                        //get the shared preferences user
                        User user = null;
                        String jsonUser = getSharedPreferences().getString(Constants
                            .KEY_LOGGED_IN_USER, null);
                        if (jsonUser != null) {
                            user = UserTypeAdapter.newInstance().fromJson(jsonUser);
                        }
                        // if there is a user saved in shared prefs
                        if (user != null) {
                            setLoggedInUser(user);
                            RxBusDriver rxBus = getRxBus();
                            rxBus.post(new SyncAllUsersCommand(rxBus, user.id().value()));
                            subscriber.onNext(user);
                        } else {
                            launchLoginActivity(activity);
                        }
                    }
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).compose(RxHelper.<User>applySchedulers());
    }
}
