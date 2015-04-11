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
import com.proxy.api.model.User;
import com.proxy.app.dialog.LoginErrorDialog;
import com.proxy.event.LoginErrorDialogEvent;
import com.squareup.otto.Subscribe;

import java.io.IOException;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

import static com.proxy.event.LoginErrorDialogEvent.DialogEvent.DISMISS;

/**
 * Activity to log in with google account.
 */
public class LoginActivity extends BaseActivity implements ConnectionCallbacks,
    OnConnectionFailedListener {

    public static final String AUTH_GOOGLE = "google";
    public static final String GOOGLE_UID_PREFIX = "google:";
    public static final String GOOGLE_ERROR_AUTH = "Error authenticating with Google: ";
    private static final int STATE_DEFAULT = 0;
    private static final int STATE_SIGN_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private static final int REQUESTCODE_SIGN_IN = 0;
    private static final String EMAIL = "https://www.googleapis.com/auth/userinfo.email";
    private static final String PROFILE = "https://www.googleapis.com/auth/userinfo.profile";
    // saved bundle strings
    private static final String SAVED_PROGRESS = "sign_in_progress";
    @InjectView(R.id.activity_login_sign_in_button)
    protected SignInButton mSignInButton;
    private Firebase mFirebaseRef;
    private boolean mGoogleIntentInProgress = false;
    // GoogleApiClient wraps our service connection to Google Play services and
    // provides access to the users sign in state and Google's APIs.
    private GoogleApiClient mGoogleApiClient;
    // We use mSignInProgress to track whether user has clicked sign in.
    // mSignInProgress can be one of three values:
    //
    //       STATE_DEFAULT: The default state of the application before the user
    //                      has clicked 'sign in', or after they have clicked
    //                      'sign out'.  In this state we will not attempt to
    //                      resolve sign in errors and so will display our
    //                      Activity in a signed out state.
    //       STATE_SIGN_IN: This state indicates that the user has clicked 'sign
    //                      in', so resolve successive errors preventing sign in
    //                      until the user has successfully authorized an account
    //                      for our app.
    //   STATE_IN_PROGRESS: This state indicates that we have started an intent to
    //                      resolve an error, and so we should not start further
    //                      intents until the current intent completes.
    private int mSignInProgress;
    // Used to store the PendingIntent most recently returned by Google Play
    // services until the user clicks 'sign in'.
    private PendingIntent mSignInIntent;
    // Used to store the error code most recently returned by Google Play services
    // until the user clicks 'sign in'.
    private int mSignInError;
    private RestClient mRestClient;

    /**
     * Sign in click listener.
     */
    @OnClick(R.id.activity_login_sign_in_button)
    protected void onClickSignIn() {
        if (!mGoogleApiClient.isConnecting()) {
            // We only process button clicks when GoogleApiClient is not transitioning
            // between connected and not connected.
            mSignInProgress = STATE_SIGN_IN;
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);
        // Button listeners
        mSignInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_DARK);
        if (savedInstanceState != null) {
            mSignInProgress = savedInstanceState
                .getInt(SAVED_PROGRESS, STATE_DEFAULT);
        }
        mRestClient = RestClient.newInstance(LoginActivity.this);
        mGoogleApiClient = buildGoogleApiClient();
                /* Create the Firebase ref that is used for all authentication with Firebase */
        mFirebaseRef = new Firebase(getResources().getString(R.string.firebase_url));
        mFirebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                if (authData != null) {
                    Timber.i("Auth State Changed");
                }
            }
        });
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
            .addScope(new Scope(EMAIL))
            .addScope(new Scope(PROFILE)).build();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_PROGRESS, mSignInProgress);
    }

    /* onConnected is called when our Activity successfully connects to Google
     * Play services.  onConnected indicates that an account was selected on the
     * device, that the selected account has granted any requested permissions to
     * our app and that we were able to establish a service connection to Google
     * Play services.
     */
    @Override
    public void onConnected(Bundle connectionHint) {
        // Update the user interface to reflect that the user is signed in.
        mSignInButton.setEnabled(false);
        getUserFromDatabase();
    }

    /**
     * Get Database {@link User}.
     */
    private void getUserFromDatabase() {
        // Retrieve some profile information to personalize our app for the user.
        Person currentUser = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
        String userId = GOOGLE_UID_PREFIX + currentUser.getId();
        mRestClient.getUserService().getUser(userId, new Callback<User>() {
            @Override
            public void success(User user, Response response) {
                Timber.i("HTTP response: " + response.getReason());
                if (user != null) {
                    ((ProxyApplication) getApplication()).setCurrentUser(user);
                    getGoogleOAuthTokenAndLogin();
                } else {
                    addUserToDatabase(createUserFromGoogle());
                }
            }

            @Override
            public void failure(RetrofitError error) {
                Timber.e(error.toString());
                addUserToDatabase(createUserFromGoogle());
            }
        });
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
        String imageURL = getUserImageURL(currentUser);

        //Create a new {@link User} with empty groups, contacts, and channels
        User user = new User();
        user.setUserId(userId);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setImageURL(imageURL);
        return user;
    }

    /**
     * Add a {@link User} to FireBase.
     *
     * @param loggedInUser the {@link User} to log in
     */
    private void addUserToDatabase(User loggedInUser) {
        mRestClient.getUserService().updateUser(loggedInUser.getUserId(),
            loggedInUser, new Callback<User>() {
                @Override
                public void success(User user, Response response) {
                    Timber.i("rest client success");
                    ((ProxyApplication) getApplication()).setCurrentUser(user);
                    getGoogleOAuthTokenAndLogin();
                }

                @Override
                public void failure(RetrofitError error) {
                    Timber.e(error.toString());
                    //TODO: Error handling for Get User retry
                }
            });
    }

    /**
     * Get a photo url with a specific size.
     *
     * @param currentUser currently logged in user
     * @return photo url String
     */
    private String getUserImageURL(Person currentUser) {
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
                    mFirebaseRef.authWithOAuthToken(AUTH_GOOGLE, token, new AuthResultHandler(
                        AUTH_GOOGLE));
                    // Indicate that the sign in process is complete.
                    mSignInProgress = STATE_DEFAULT;
                    IntentLauncher.launchMainActivity(LoginActivity.this);
                    finish();
                } else if (errorMessage != null) {
                    createErrorDialog(errorMessage).show(getSupportFragmentManager());
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
            Timber.w("API Unavailable.");
        } else if (mSignInProgress != STATE_IN_PROGRESS) {
            // We do not have an intent in progress so we should store the latest
            // error resolution intent for use when the sign in button is clicked.
            mSignInIntent = result.getResolution();
            mSignInError = result.getErrorCode();

            if (mSignInProgress == STATE_SIGN_IN) {
                // STATE_SIGN_IN indicates the user already clicked the sign in button
                // so we should continue processing errors until the user is signed in
                // or they click cancel.
                resolveSignInError();
            }
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
                mSignInProgress = STATE_IN_PROGRESS;
                startIntentSenderForResult(mSignInIntent.getIntentSender(),
                    REQUESTCODE_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                Timber.i("Sign in intent could not be sent: " + e.getLocalizedMessage());
                // The intent was canceled before it was sent.  Attempt to connect to
                // get an updated ConnectionResult.
                mSignInProgress = STATE_SIGN_IN;
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
                            mSignInProgress = STATE_DEFAULT;
                        }
                    });
            } else {
                createErrorDialog("google error").show(getSupportFragmentManager());
            }
        }
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        mGoogleIntentInProgress = false;
        switch (requestCode) {
            case REQUESTCODE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    // If the error resolution was successful we should continue
                    // processing errors.
                    mSignInProgress = STATE_SIGN_IN;
                } else {
                    // If the error resolution was not successful or the user canceled,
                    // we should stop processing errors.
                    mSignInProgress = STATE_DEFAULT;
                }

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
     * Return log in error dialog based on the type of error.
     *
     * @param message error message
     * @return error dialog
     */
    private LoginErrorDialog createErrorDialog(String message) {
        return LoginErrorDialog.newInstance("Login Error", message);
    }

    /**
     * An action occured with the error dialog created in {@link LoginActivity#createErrorDialog}.
     *
     * @param event dialog action
     */
    @Subscribe
    @SuppressWarnings("unused")
    protected void errorDialogAction(LoginErrorDialogEvent event) {
        if (event.action.equals(DISMISS)) {
            mSignInProgress = STATE_DEFAULT;
        }
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        private final String provider;

        /**
         * Constructor.
         *
         * @param provider auth provider
         */
        public AuthResultHandler(String provider) {
            this.provider = provider;
        }

        @Override
        public void onAuthenticated(AuthData authData) {
            Timber.i(provider + authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            createErrorDialog(firebaseError.toString()).show(getSupportFragmentManager());
        }
    }

}
