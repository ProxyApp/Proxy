package com.shareyourproxy.app;

import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.shareyourproxy.Constants;
import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.fragment.DrawerFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;


/**
 * The main landing point after loggin in. This is tabbed activity with {@link Contact}s and {@link
 * Group}s.
 */
public class MainActivity extends GoogleApiActivity {
    private GoogleApiClient _googleApiClient;
    private CompositeSubscription _subscriptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _googleApiClient = getGoogleApiClient();
        if (savedInstanceState == null) {
            MainFragment mainFragment = MainFragment.newInstance();
            DrawerFragment drawerFragment = DrawerFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_fragment_container, mainFragment)
                .replace(R.id.activity_main_drawer_fragment_container, drawerFragment)
                .commit();
        }
    }

    /**
     * {@link SelectDrawerItemEvent}. When a drawer item is selected, call a proper event flow.
     *
     * @param event data
     */
    public void onDrawerItemSelected(SelectDrawerItemEvent event) {
        //if the user presses logout
        if (getString(R.string.profile).equals(event.message)) {
            User user = getLoggedInUser();
            IntentLauncher.launchUserProfileActivity(this, user, user.id().value());
        } else if (getString(R.string.logout).equals(event.message)) {
            // and the google api is connected
            if (_googleApiClient.isConnected()) {
                clearValuesAndLogout();
            } else {
                Toast.makeText(MainActivity.this, "Not Connected To Google Service, Try Again"
                    , Toast.LENGTH_SHORT).show();
                _googleApiClient.connect();
            }
        } else if (getString(R.string.about).equals(event.message)) {
            IntentLauncher.launchAboutActivity(this);
        } else if (getString(R.string.report_problem).equals(event.message)) {
            IntentLauncher.launchEmailIntent(this, getString(R.string.contact_proxy));
        } else if (getString(R.string.invite_friend).equals(event.message)) {
            IntentLauncher.launchInviteFriendIntent(this);
        }
    }

    /**
     * Clear saved user data and finish this activity.
     */
    public void clearValuesAndLogout() {
        setLoggedInUser(null);
        Plus.AccountApi.clearDefaultAccount(_googleApiClient);
        IntentLauncher.launchLoginActivity(this);
        deleteRealm();
        getSharedPreferences().edit().remove(Constants.KEY_LOGGED_IN_USER).commit();
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof SelectDrawerItemEvent) {
                        onDrawerItemSelected((SelectDrawerItemEvent) event);
                    }
                }
            }));
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        try {
            return super.dispatchTouchEvent(ev);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Timber.i("Connected to G+");
    }

    @Override
    public void onConnectionSuspended(int i) {
        _googleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        _googleApiClient.connect();
    }


    @Override
    public void onAuthenticated(AuthData authData) {

    }

    @Override
    public void onAuthenticationError(Throwable e) {

    }

}


