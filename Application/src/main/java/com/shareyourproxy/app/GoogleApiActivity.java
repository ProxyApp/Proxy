package com.shareyourproxy.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.plus.Plus;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.R;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.api.rx.RxHelper;
import com.shareyourproxy.api.rx.RxLoginHelper;
import com.shareyourproxy.api.rx.RxLoginHelper.AuthResultHandler;
import com.shareyourproxy.api.rx.event.RefreshFirebaseAuthenticationEvent;
import com.shareyourproxy.app.dialog.ErrorDialog;

import java.io.IOException;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

import static com.shareyourproxy.Constants.PROVIDER_GOOGLE;

/**
 * Base abstraction for classes to inherit common google plus login callbacks and functions.
 */
public abstract class GoogleApiActivity extends BaseActivity implements
    ConnectionCallbacks, OnConnectionFailedListener {
    public static final String GOOGLE_UID_PREFIX = "google:";
    public static final int REQUESTCODE_SIGN_IN = 0;
    private static final String GOOGLE_ERROR_AUTH = "Error authenticating with Google: %1$s";
    private static final Firebase _firebaseRef = new Firebase(BuildConfig.FIREBASE_ENDPOINT);
    private AuthResultHandler _authResultHandler = new AuthResultHandler(this);
    private boolean _googleIntentInProgress = false;
    private CompositeSubscription _subscriptions;
    private boolean _isTokenRefreshing = false;
    private JustObserver _tokenRefreshObservable;
    private GoogleApiClient _googleApiClient;
    private RxHelper rxHelper = RxHelper.INSTANCE;

    /**
     * Return log in onError dialog based on the type of onError.
     *
     * @param message onError message
     */
    public static void showErrorDialog(BaseActivity activity, String message) {
        if (message == null || message.trim().isEmpty()) {
            message = String.format(GOOGLE_ERROR_AUTH, "null error message");
        }
        ErrorDialog.newInstance(activity.getString(R.string.login_error), message)
            .show(activity.getSupportFragmentManager());
    }

    private static GoogleApiClient buildGoogleApiClient(GoogleApiActivity activity) {
        return new GoogleApiClient.Builder(activity)
            .addConnectionCallbacks(activity)
            .addOnConnectionFailedListener(activity)
            .addApi(Plus.API, Plus.PlusOptions.builder().build())
            .addScope(Plus.SCOPE_PLUS_LOGIN)
            .addScope(new Scope(Scopes.PLUS_ME))
            .addScope(new Scope(SCOPE_EMAIL))
            .addScope(Plus.SCOPE_PLUS_PROFILE).build();
    }

    public abstract void onAuthenticated(AuthData authData);

    public abstract void onAuthenticationError(Throwable e);

    /**
     * Get the authentication token from google plus so we can log into firebase.
     */
    public Subscription loginToFirebaseSubscription(final GoogleApiActivity activity) {
        return RxLoginHelper.INSTANCE.refreshGooglePlusToken(activity, _googleApiClient)
            .compose(rxHelper.<String>observeMain()).subscribe(loginObserver(activity));
    }

    public JustObserver<String> loginObserver(final GoogleApiActivity activity) {
        return new JustObserver<String>() {

            @Override
            public void next(String token) {
                _firebaseRef.authWithOAuthToken(PROVIDER_GOOGLE, token, _authResultHandler);
            }

            @Override
            public void error(Throwable e) {
                if (e instanceof IOException) {
                    Timber.e(GOOGLE_ERROR_AUTH, Log.getStackTraceString(e));
                } else if (e instanceof UserRecoverableAuthException) {
                    Timber.e("Recoverable Google OAuth onError: %1$s", Log.getStackTraceString(e));
                    if (!_googleIntentInProgress) {
                        _googleIntentInProgress = true;
                        Intent recover = ((UserRecoverableAuthException) e).getIntent();
                        startActivityForResult(recover, REQUESTCODE_SIGN_IN);
                    }
                } else if (e instanceof GoogleAuthException) {
                    Timber.e(GOOGLE_ERROR_AUTH, Log.getStackTraceString(e));
                }
                activity.onAuthenticationError(e);
            }
        };
    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        _googleIntentInProgress = false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _googleApiClient = buildGoogleApiClient(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        _subscriptions = new CompositeSubscription();
        _subscriptions.add(getRxBus().toObservable()
            .subscribe(onNextEvent(this)));
    }

    private JustObserver<Object> onNextEvent(final GoogleApiActivity activity) {
        return new JustObserver<Object>() {
            @Override
            public void next(Object event) {
                if (event instanceof RefreshFirebaseAuthenticationEvent) {
                    if (!_isTokenRefreshing) {
                        _isTokenRefreshing = true;
                        _subscriptions.add(RxLoginHelper.INSTANCE.refreshFirebaseAuth(activity,
                            getGoogleApiClient(), getSharedPreferences())
                            .subscribe(getTokenRefreshObserver()));
                    }
                }
            }
        };
    }

    public JustObserver<String> getTokenRefreshObserver() {
        if (_tokenRefreshObservable == null) {
            _tokenRefreshObservable = new JustObserver<String>() {
                @Override
                public void next(String token) {
                    getSharedPreferences().edit()
                        .putString(Constants.KEY_GOOGLE_PLUS_AUTH, token)
                        .commit();
                    _isTokenRefreshing = false;
                }
            };
        }
        return _tokenRefreshObservable;
    }

    @Override
    protected void onPause() {
        super.onPause();
        _subscriptions.unsubscribe();
        _subscriptions = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // let the user press the button to connect to gms on the login activity
        if (!(this instanceof LoginActivity)) {
            _googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        _googleApiClient.disconnect();
    }

    public GoogleApiClient getGoogleApiClient() {
        return _googleApiClient;
    }

}
