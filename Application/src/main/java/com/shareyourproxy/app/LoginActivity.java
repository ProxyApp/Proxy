package com.shareyourproxy.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.client.AuthData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.android.gms.plus.model.people.Person.Cover.CoverPhoto;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.command.AddUserCommand;
import com.shareyourproxy.api.rx.command.SyncAllUsersCommand;
import com.shareyourproxy.api.rx.event.SyncAllUsersErrorEvent;
import com.shareyourproxy.api.rx.event.SyncAllUsersSuccessEvent;
import com.shareyourproxy.app.fragment.MainFragment;

import java.util.HashMap;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.BuildConfig.VERSION_CODE;
import static com.shareyourproxy.Constants.KEY_PLAYED_INTRODUCTION;
import static com.shareyourproxy.IntentLauncher.launchIntroductionActivity;
import static com.shareyourproxy.IntentLauncher.launchMainActivity;
import static com.shareyourproxy.util.ViewUtils.dpToPx;
import static com.shareyourproxy.util.ViewUtils.svgToBitmapDrawable;


/**
 * Log in with a google plus account.
 */
public class LoginActivity extends GoogleApiActivity {

    // View
    @Bind(R.id.activity_login_title)
    TextView proxyLogo;
    @Bind(R.id.activity_login_sign_in_button)
    SignInButton signInButton;
    @BindDimen(R.dimen.common_rect_tiny)
    int margin;
    // Transient
    private PendingIntent _signInIntent;
    private int _signInError;
    private CompositeSubscription _subscriptions;
    private GoogleApiClient _googleApiClient;


    /**
     * Sign in click listener.
     */
    @OnClick(R.id.activity_login_sign_in_button)
    protected void onClickSignIn() {
        if (!_googleApiClient.isConnecting()) {
            _googleApiClient.connect();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        initialize();
    }

    private void initialize() {
        initializeValues();
        drawLogo();
    }

    private void initializeValues() {
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        signInButton.setEnabled(true);
        _googleApiClient = getGoogleApiClient();
    }

    /**
     * Set the Logo image.drawable on this activities {@link ImageView}.
     */
    private void drawLogo() {
        Drawable draw = svgToBitmapDrawable(this,
            R.raw.ic_proxy_logo_typed, (int) getResourceDimension(this));
        proxyLogo.setCompoundDrawablesWithIntrinsicBounds(null, draw, null, null);
    }

    /**
     * Get a big icon dimension size.
     *
     * @param activity context
     * @return resource dimension
     */
    private float getResourceDimension(Activity activity) {
        return dpToPx(activity.getResources(), R.dimen.common_svg_ultra_minor);
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(getRxBusObserver()));
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
                showErrorDialog(LoginActivity.this, getString(R.string.rx_eventbus_error));
                signInButton.setEnabled(true);
            }
        };
    }

    public void login() {
        if (!getSharedPreferences().contains(KEY_PLAYED_INTRODUCTION)) {
            launchIntroductionActivity(this);
        } else {
            launchMainActivity(this, MainFragment.ARG_SELECT_CONTACTS_TAB, false, null);
        }
        finish();
    }

    @Override
    public void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (_googleApiClient.isConnected()) {
            _googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        loginToFirebase(this);
    }

    /**
     * Get Database {@link User}.
     */
    private void getUserFromDatabase() {
        // Retrieve some profile information to personalize our app for the user.
        signInButton.setEnabled(false);
        Person currentUser = Plus.PeopleApi.getCurrentPerson(_googleApiClient);
        if (currentUser != null) {
            String userId = GOOGLE_UID_PREFIX + currentUser.getId();
            RestClient.getUserService(this, getRxBus()).getUser(userId)
                .compose(RxHelper.<User>applySchedulers())
                .subscribe(getUserObserver());
        } else {
            showErrorDialog(this, getString(R.string.login_error_retrieving_user));
            signInButton.setEnabled(true);
            if (_googleApiClient.isConnected()) {
                _googleApiClient.disconnect();
            }
        }
    }

    private JustObserver<User> getUserObserver() {
        return new JustObserver<User>() {
            @Override
            public void success(User user) {
                if (user == null) {
                    addUserToDatabase(createUserFromGoogle());
                } else {
                    setLoggedInUser(user);
                    RestClient.getUserService(LoginActivity.this, getRxBus())
                        .updateUserVersion(user.id().value(), BuildConfig.VERSION_CODE).subscribe();
                    getRxBus().post(new SyncAllUsersCommand(getRxBus(), user.id().value()));
                }
            }

            @Override
            public void error(Throwable e) {
                showErrorDialog(
                    LoginActivity.this, getString(R.string.retrofit_general_error));
                signInButton.setEnabled(true);
            }
        };
    }

    /**
     * Create a User from their google profile.
     *
     * @return created user
     */
    private User createUserFromGoogle() {
        // Retrieve some profile information to personalize our app for the user.
        Person currentUser = Plus.PeopleApi.getCurrentPerson(_googleApiClient);
        String userUID = GOOGLE_UID_PREFIX + currentUser.getId();
        String firstName = currentUser.getName().getGivenName();
        String lastName = currentUser.getName().getFamilyName();
        String email = Plus.AccountApi.getAccountName(_googleApiClient);
        String profileURL = getLargeImageURL(currentUser);
        Person.Cover cover = currentUser.getCover();
        String coverURL = null;
        Timber.e("has cover:" + currentUser.hasCover());
        if (cover != null) {
            CoverPhoto coverPhoto = cover.getCoverPhoto();
            if (coverPhoto != null) {
                coverURL = coverPhoto.getUrl();
            }
        }
        //Create a new {@link User} with empty groups, contacts, and channels
        Id id = Id.builder().value(userUID).build();
        return User.create(id, firstName, lastName, email, profileURL, coverURL,
            null, getDefaultGroups(), null, VERSION_CODE);
    }

    private HashMap<String, Group> getDefaultGroups() {
        HashMap<String, Group> groups = new HashMap<>(3);
        String[] groupLabels = getResources().getStringArray(R.array.default_groups);
        for (String label : groupLabels) {
            Group group = Group.create(label);
            groups.put(group.id().value(), group);
        }
        return groups;
    }

    /**
     * Add a {@link User} to FireBase.
     *
     * @param newUser the {@link User} to log in
     */
    private void addUserToDatabase(User newUser) {
        setLoggedInUser(newUser);
        getRxBus().post(new AddUserCommand(getRxBus(), newUser));
    }

    /**
     * Get a photo url with a larger size than the default return value.
     *
     * @param currentUser currently logged in user
     * @return photo url String
     */
    private String getLargeImageURL(Person currentUser) {
        return currentUser.getImage().getUrl().replace("?sz=50", "?sz=200");
    }

    /**
     * onConnectionFailed is called when our Activity could not connect to Google Play services.
     * onConnectionFailed indicates that the user needs to select an account, grant permissions or
     * resolve an onError in order to sign in.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what onError codes might
        // be returned in onConnectionFailed.
        Timber.i("onConnectionFailed: ConnectionResult.getErrorCode() = " + result.getErrorCode());

        if (result.getErrorCode() == ConnectionResult.API_UNAVAILABLE) {
            // An API requested for GoogleApiClient is not available. The device's current
            // configuration might not be supported with the requested API or a required component
            // may not be installed, such as the Android Wear application. You may need to use a
            // second GoogleApiClient to manage the application's optional APIs.
            String error = getString(R.string.login_error_api_unavailable);
            Timber.w(error);
            showErrorDialog(this, error);
            signInButton.setEnabled(true);
        } else if (result.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showErrorDialog(this, getString(R.string.login_error_update_play_service));
            signInButton.setEnabled(true);
        } else {
            // We do not have an intent in progress so we should store the latest
            // onError resolution intent for use when the sign in button is clicked.
            _signInIntent = result.getResolution();
            _signInError = result.getErrorCode();
            Timber.i(String.valueOf(_signInError));
            // STATE_SIGN_IN indicates the user already clicked the sign in button
            // so we should continue processing errors until the user is signed in
            // or they click cancel.
            resolveSignInError();
        }
    }

    /**
     * Starts an appropriate intent or dialog for user interaction to resolve the current onError
     * preventing the user from being signed in.  This could be a dialog allowing the user to select
     * an account, an activity allowing the user to consent to the permissions being requested by
     * your app, a setting to enable device networking, etc.
     */
    private void resolveSignInError() {
        if (_signInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an onError.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the onError currently preventing our connection to
                // Google Play services.
                startIntentSenderForResult(_signInIntent.getIntentSender(),
                    REQUESTCODE_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Timber.i("Sign in intent could not be sent: " + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                _googleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // onError types, so we show the default Google Play services onError
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            if (GooglePlayServicesUtil.isUserRecoverableError(_signInError)) {
                GooglePlayServicesUtil.getErrorDialog(_signInError, this, REQUESTCODE_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Timber.e("Google Play services resolution cancelled");
                        }
                    });
            } else {
                showErrorDialog(this, getString(R.string.login_error_failed_connection));
                signInButton.setEnabled(true);
                if (_googleApiClient.isConnected()) {
                    _googleApiClient.disconnect();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        if (!_googleApiClient.isConnecting()) {
            _googleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        _googleApiClient.connect();
    }

    @Override
    public void onAuthenticated(AuthData authData) {
        getUserFromDatabase();
    }

    @Override
    public void onAuthenticationError(Throwable e) {
        showErrorDialog(this, e.toString());
        signInButton.setEnabled(true);
    }
}
