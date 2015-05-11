package com.proxy.app;

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
import com.proxy.IntentLauncher;
import com.proxy.ProxyApplication;
import com.proxy.R;
import com.proxy.api.RestClient;
import com.proxy.api.domain.model.User;
import com.proxy.app.dialog.LoginErrorDialog;

import java.io.IOException;
import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import io.realm.Realm;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import static com.proxy.api.domain.factory.UserFactory.createRealmUser;

/**
 * Activity to log in with google account.
 */
public class LoginActivity extends BaseActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {
    public static final String PROVIDER_GOOGLE = "google";
    public static final String GOOGLE_UID_PREFIX = "google:";
    public static final String GOOGLE_ERROR_AUTH = "Error authenticating with Google: ";
    private static final int REQUESTCODE_SIGN_IN = 0;
    private static final String SCOPE_EMAIL = "https://www.googleapis.com/auth/userinfo.email";
    private static final String SCOPE_PROFILE = "https://www.googleapis.com/auth/userinfo.profile";
    // Views
    @InjectView(R.id.activity_login_sign_in_button)
    protected SignInButton mSignInButton;
    private AuthResultHandler mAuthResultHandler = new AuthResultHandler(
        new WeakReference<>(this), PROVIDER_GOOGLE);
    private Firebase mFirebaseRef;
    private boolean mGoogleIntentInProgress = false;
    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient mGoogleApiClient;
    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;
    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;
    private RestClient mRestClient;
    private Realm mRealm;

    /**
     * Return log in error dialog based on the type of error.
     *
     * @param message error message
     */
    private static void showErrorDialog(BaseActivity activity, String message) {
        LoginErrorDialog.newInstance(activity.getString(R.string.login_error), message)
            .show(activity.getSupportFragmentManager());
    }

    /**
     * Sign in click listener.
     */
    @OnClick(R.id.activity_login_sign_in_button)
    protected void onClickSignIn() {
        if (mGoogleApiClient.isConnected()) {
            getUserFromDatabase();
        } else if (!mGoogleApiClient.isConnecting()) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = getDefaultRealm();
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        // Button listeners
        mSignInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        mSignInButton.setEnabled(true);
        mRestClient = RestClient.newInstance(LoginActivity.this);
        mGoogleApiClient = buildGoogleApiClient();
                /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
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
            .addScope(new Scope(SCOPE_PROFILE)).build();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
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
        mSignInButton.setEnabled(false);
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        if (currentUser != null) {
            String userId = GOOGLE_UID_PREFIX + currentUser.getId();
            mRestClient.getUserService().getUser(userId, new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    Timber.i("HTTP response: " + response.getReason());
                    if (user == null) {
                        addUserToDatabase(createUserFromGoogle());
                    } else {
                        setUserAndAuth(user);
                    }
                }

                @Override
                public void failure(RetrofitError error) {
                    Timber.e(error.toString());
                    showErrorDialog(LoginActivity.this, error.getMessage());
                    mSignInButton.setEnabled(true);
                }
            });
        } else {
            showErrorDialog(this, getString(R.string.login_error_retrieving_user));
            mSignInButton.setEnabled(true);
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
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
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        String userId = GOOGLE_UID_PREFIX + currentUser.getId();
        String firstName = currentUser.getName().getGivenName();
        String lastName = currentUser.getName().getFamilyName();
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        String imageURL = getLargeImageURL(currentUser);

        //Create a new {@link User} with empty groups, contacts, and channels
        return User.create(userId, firstName, lastName, email, imageURL, null, null, null);
    }

    /**
     * Add a {@link User} to FireBase.
     *
     * @param loggedInUser the {@link User} to log in
     */
    private void addUserToDatabase(User loggedInUser) {
        mRestClient.getUserService().updateUser(loggedInUser.userId(),
            loggedInUser, new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    Timber.i("rest client success");
                    setUserAndAuth(user);
                }

                @Override
                public void failure(RetrofitError error) {
                    Timber.e(error.toString());
                    //TODO: Error handling for Get User retry
                }
            });
    }

    /**
     * Set the user in {@link ProxyApplication}.
     *
     * @param user to login
     */
    private void setUserAndAuth(final User user) {
        setLoggedInUser(user);
        transactRealmObject(mRealm, createRealmUser(user));
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
                        .getAccountName(mGoogleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Timber.e(GOOGLE_ERROR_AUTH + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Timber.w("Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is
                    none pending */
                    if (!mGoogleIntentInProgress) {
                        mGoogleIntentInProgress = true;
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
                    mFirebaseRef.authWithOAuthToken(PROVIDER_GOOGLE, token, mAuthResultHandler);
                } else if (errorMessage != null) {
                    showErrorDialog(LoginActivity.this, errorMessage);
                }
            }
        };
        task.execute();
    }

    /* onConnectionFailed is called when our Activity could not connect to Google
     * Play services.  onConnectionFailed indicates that the user needs to select
     * an account, grant permissions or resolve an error in order to sign in.
     */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Refer to the javadoc for ConnectionResult to see what error codes might
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
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();
            Timber.i(String.valueOf(mSignInError));
            // STATE_SIGN_IN indicates the user already clicked the sign in button
            // so we should continue processing errors until the user is signed in
            // or they click cancel.
            resolveSignInError();
        }
    }

    /**
     * Starts an appropriate intent or dialog for user interaction to resolve the current error
     * preventing the user from being signed in.  This could be a dialog allowing the user to select
     * an account, an activity allowing the user to consent to the permissions being requested by
     * your app, a setting to enable device networking, etc.
     */
    private void resolveSignInError() {
        if (mSignInIntent != null) {
            // We have an intent which will allow our user to sign in or
            // resolve an error.  For example if the user needs to
            // select an account to sign in with, or if they need to consent
            // to the permissions your app is requesting.

            try {
                // Send the pending intent that we stored on the most recent
                // OnConnectionFailed callback.  This will allow the user to
                // resolve the error currently preventing our connection to
                // Google Play services.
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                    REQUESTCODE_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Timber.i("Sign in intent could not be sent: " + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mGoogleApiClient.connect();
            }
        } else {
            // Google Play services wasn't able to provide an intent for some
            // error types, so we show the default Google Play services error
            // dialog which may still start an intent on our behalf if the
            // user can resolve the issue.
            if (GooglePlayServicesUtil.isUserRecoverableError(mSignInError)) {
                GooglePlayServicesUtil.getErrorDialog(mSignInError, this, REQUESTCODE_SIGN_IN,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Timber.e("Google Play services resolution cancelled");
                        }
                    });
            } else {
                showErrorDialog(this, getString(R.string.login_error_failed_connection));
                if (mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.disconnect();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        mGoogleIntentInProgress = false;
        switch (requestCode) {
            case REQUESTCODE_SIGN_IN:
                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
            default:
                if (!mGoogleApiClient.isConnecting()) {
                    // If Google Play services resolved the issue with a dialog then
                    // onStart is not called so we need to re-attempt connection here.
                    mGoogleApiClient.connect();
                }
                break;
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason.
        // We call connect() to attempt to re-establish the connection or get a
        // ConnectionResult that we can attempt to resolve.
        mGoogleApiClient.connect();
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
