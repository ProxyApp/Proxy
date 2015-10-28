package com.shareyourproxy.app;

import android.os.Bundle;
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
import com.shareyourproxy.api.rx.RxGoogleAnalytics;
import com.shareyourproxy.api.rx.event.SelectDrawerItemEvent;
import com.shareyourproxy.app.dialog.ShareLinkDialog;
import com.shareyourproxy.app.fragment.DrawerFragment;
import com.shareyourproxy.app.fragment.MainFragment;

import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.IntentLauncher.launchEmailIntent;
import static com.shareyourproxy.IntentLauncher.launchIntroductionActivity;
import static com.shareyourproxy.IntentLauncher.launchInviteFriendIntent;
import static com.shareyourproxy.IntentLauncher.launchUserProfileActivity;


/**
 * The main landing point after loggin in. This is tabbed activity with {@link Contact}s and {@link
 * Group}s.
 */
public class MainActivity extends GoogleApiActivity {
    private GoogleApiClient _googleApiClient;
    private CompositeSubscription _subscriptions;
    private final RxGoogleAnalytics _analytics = RxGoogleAnalytics.getInstance(this);

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
        switch (event.drawerItem) {
            case PROFILE:
                User user = getLoggedInUser();
                _analytics.userProfileViewed(user);
                launchUserProfileActivity(this, user, user.id());
                break;
            case SHARE_PROFILE:
                ShareLinkDialog.newInstance(getLoggedInUser().groups())
                    .show(getSupportFragmentManager());
                break;
            case INVITE_FRIEND:
                launchInviteFriendIntent(this);
                break;
            case TOUR:
                launchIntroductionActivity(this);
                break;
            case REPORT_ISSUE:
                launchEmailIntent(this, getString(R.string.contact_proxy));
                break;
            case LOGOUT:
                logout();
                break;
            case HEADER:
                //nada
                break;
            default:
                Timber.e("Invalid drawer item");
                break;
        }
    }

    /**
     * Log out the logged in the user and go back to the LoginActivity.
     */
    public void logout() {
        // and the google api is connected
        if (_googleApiClient.isConnected()) {
            clearValuesAndLogout();
        } else {
            Toast.makeText(MainActivity.this, "Not Connected To Google Service, Try Again"
                , Toast.LENGTH_SHORT).show();
            _googleApiClient.connect();
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


