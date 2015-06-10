package com.shareyourproxy.app;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.shareyourproxy.IntentLauncher;
import com.shareyourproxy.ProxyApplication;
import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.command.AddUserCommand;
import com.shareyourproxy.api.rx.command.callback.UserSavedEvent;
import com.shareyourproxy.app.dialog.ErrorDialog;

import java.io.IOException;
import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static rx.android.app.AppObservable.bindActivity;

/**
 * Activity to log in with google account.
 */
public class LoginActivity extends BaseActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {

    private static final int REQUESTCODE_SIGN_IN = 0;
    private static final String PROVIDER_GOOGLE = "google";
    private static final String GOOGLE_UID_PREFIX = "google:";
    private static final String GOOGLE_ERROR_AUTH = "Error authenticating with Google: ";
    private static final String SCOPE_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
    // Views
    @InjectView(R.id.activity_login_sign_in_button)
    protected SignInButton signInButton;
    private AuthResultHandler _authResultHandler = new AuthResultHandler(
        new WeakReference<>(this), PROVIDER_GOOGLE);
    private Firebase _firebaseRef;
    private boolean _googleIntentInProgress = false;
    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient _googleApiClient;
    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent _signInIntent;
    // Used to store the onError code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int _signInError;
    private CompositeSubscription _subscriptions;

    /**
     * Return log in onError dialog based on the type of onError.
     *
     * @param message onError message
     */
    private static void showErrorDialog(BaseActivity activity, String message) {
        ErrorDialog.newInstance(activity.getString(R.string.login_error), message)
            .show(activity.getSupportFragmentManager());
    }

    /**
     * Sign in click listener.
     */
    @OnClick(R.id.activity_login_sign_in_button)
    protected void onClickSignIn() {
        if (_googleApiClient.isConnected()) {
            getUserFromDatabase();
        } else if (!_googleApiClient.isConnecting()) {
            _googleApiClient.connect();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        // Button listeners
        signInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        signInButton.setEnabled(true);
        _googleApiClient = buildGoogleApiClient();
                /* Create the Firebase ref that is used for all authentication with Firebase */
        _firebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
    }

    /**
     * When we build the GoogleApiClient we specify where connected and connection failed callbacks
     * should be returned, which Google APIs our app uses and which OAuth 2.0 scopes our app
     * requests.
     *
     * @return Api Client
     */
    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(Plus.API, Plus.PlusOptions.builder().build())
            .addScope(Plus.SCOPE_PLUS_LOGIN)
            .addScope(new Scope(SCOPE_EMAIL))
            .addScope(Plus.SCOPE_PLUS_PROFILE).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        _googleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(bindActivity(this, getRxBus().toObserverable())//
            .subscribe(new Action1<Object>() {
                @Override
                public void call(Object event) {
                    if (event instanceof UserSavedEvent) {
                        userAddedToFirebase((UserSavedEvent) event);
                    }
                }
            }));
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

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Update the user interface to reflect that the user is signed in
        getUserFromDatabase();
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
            RestClient.getUserService(this).getUser(userId)
                .compose(RxHelper.<User>applySchedulers())
                .subscribe(new JustObserver<User>() {
                    @Override
                    public void onError() {
                        showErrorDialog(LoginActivity.this, "Retrofit general onError getting " +
                            "User");
                        signInButton.setEnabled(true);
                    }

                    @Override
                    public void onNext(User user) {
                        if (user == null) {
                            addUserToDatabase(createUserFromGoogle());
                        } else {
                            setLoggedInUser(user);
                            authUser(user);
                        }
                    }
                });
        } else {
            showErrorDialog(this, getString(R.string.login_error_retrieving_user));
            signInButton.setEnabled(true);
            if (_googleApiClient.isConnected()) {
                _googleApiClient.disconnect();
            }
        }
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
        String imageURL = getLargeImageURL(currentUser);

        //Create a new {@link User} with empty contactGroups, contacts, and channels
        Id id = Id.builder().value(userUID).build();
        return User.create(id, firstName, lastName, email, imageURL, null, null, null);
    }

    /**
     * Add a {@link User} to FireBase.
     *
     * @param newUser the {@link User} to log in
     */
    private void addUserToDatabase(User newUser) {
        setLoggedInUser(newUser);
        getRxBus().post(new AddUserCommand(newUser));
    }

    private void userAddedToFirebase(UserSavedEvent event) {
        authUser(event.user);
//TODO: ERROR HANDLING FOR SAVING A USER
//                signInButton.setEnabled(true);
//                showErrorDialog(LoginActivity.this, "Retrofit general onError saving User");
    }

    /**
     * Set the user in {@link ProxyApplication}.
     *
     * @param user to login
     */
    private void authUser(final User user) {
        getGoogleOAuthTokenAndLogin();
    }

    /**
     * Get a photo url with a larger size than the defualt return value.
     *
     * @param currentUser currently logged in user
     * @return photo url String
     */
    private String getLargeImageURL(Person currentUser) {
        return currentUser.getImage().getUrl().replace("?sz=50", "?sz=200");
    }

    /**
     * Get the authentication token from google plus so we can log into firebase.
     */
    private void getGoogleOAuthTokenAndLogin() {
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;
                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(LoginActivity.this, Plus.AccountApi
                        .getAccountName(_googleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server onError */
                    Timber.e(GOOGLE_ERROR_AUTH + transientEx);
                    errorMessage = "Network onError: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Timber.w("Recoverable Google OAuth onError: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is
                    none pending */
                    if (!_googleIntentInProgress) {
                        _googleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        startActivityForResult(recover, REQUESTCODE_SIGN_IN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already
                    verified that
                     * Google Play services is installed. */
                    Timber.e(GOOGLE_ERROR_AUTH + authEx.getMessage(), authEx);
                    errorMessage = GOOGLE_ERROR_AUTH + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                if (token != null) {
                    /* Successfully got OAuth token, now login with Google */
                    _firebaseRef.authWithOAuthToken(PROVIDER_GOOGLE, token, _authResultHandler);
                } else if (errorMessage != null) {
                    showErrorDialog(LoginActivity.this, errorMessage);
                }
            }
        };
        task.execute();
    }

    /* onConnectionFailed is called when our Activity could not connect to Google
     * Play services.  onConnectionFailed indicates that the user needs to select
     * an account, grant permissions or resolve an onError in order to sign in.
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
        } else if (result.getErrorCode() == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            showErrorDialog(this, getString(R.string.login_error_update_play_service));
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
                if (_googleApiClient.isConnected()) {
                    _googleApiClient.disconnect();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        _googleIntentInProgress = false;
        switch (requestCode) {
            case REQUESTCODE_SIGN_IN:
                if (!_googleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    _googleApiClient.connect();
                }
                break;
            default:
                if (!_googleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    _googleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        _googleApiClient.connect();
    }

    /**
     * Utility class for authentication results
     */
    private static class AuthResultHandler implements Firebase.AuthResultHandler {
        private final WeakReference<LoginActivity> activity;
        private final String provider;

        /**
         * Constructor.
         *
         * @param activity context
         * @param provider auth provider
         */
        public AuthResultHandler(WeakReference<LoginActivity> activity, String provider) {
            this.provider = provider;
            this.activity = activity;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            Timber.i(provider + authData);
            IntentLauncher.launchMainActivity(activity.get());
            activity.get().finish();
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            showErrorDialog(activity.get(), firebaseError.toString());
        }
    }

}
