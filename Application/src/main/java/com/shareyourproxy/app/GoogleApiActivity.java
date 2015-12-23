package com.shareyourproxy.app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.plus.model.people.Person;
import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.JustObserver;
import com.shareyourproxy.app.dialog.ErrorDialog;

import java.util.HashMap;

import static com.shareyourproxy.BuildConfig.VERSION_CODE;

/**
 * Base abstraction for classes to inherit common google plus login callbacks and functions.
 */
public abstract class GoogleApiActivity extends BaseActivity implements ConnectionCallbacks, OnConnectionFailedListener {
    public static final String GOOGLE_UID_PREFIX = "google:";
    private static final int RC_SIGN_IN = 0;
    private static final String GOOGLE_ERROR_AUTH = "Error authenticating with Google: %1$s";
    private static GoogleSignInOptions OPTIONS = getSignInOptions();
    private JustObserver<String> _tokenRefreshObservable;
    private GoogleApiClient _googleApiClient;

    @NonNull
    private static GoogleSignInOptions getSignInOptions() {
        return new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestEmail().build();
    }

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
            .enableAutoManage(activity, activity)
            .addApi(Auth.GOOGLE_SIGN_IN_API, OPTIONS).build();
    }

    public void onGooglePlusSignIn(GoogleSignInAccount acct){}

    public void onGooglePlusError(Status status){}

    protected void signInToGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(_googleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Create a User from their google profile.
     *
     * @return created user
     */
    protected User createUserFromGoogle(GoogleSignInAccount acct) {
        Uri userPhoto = acct.getPhotoUrl();
        String id = acct.getId();

        // Retrieve some profile information to personalize our app for the user.
        Person currentUser = RestClient.INSTANCE.getHerokuUserService().getCurrentPerson(id).toBlocking().single();
        String userId = new StringBuilder(GOOGLE_UID_PREFIX).append(id).toString();
        String firstName = currentUser.getName().getGivenName();
        String lastName = currentUser.getName().getFamilyName();
        String email = acct.getEmail();
        String profileURL = userPhoto != null ? userPhoto.toString():null;
        Person.Cover cover = currentUser.getCover();
        String coverURL = null;
        if (cover != null) {
            Person.Cover.CoverPhoto coverPhoto = cover.getCoverPhoto();
            if (coverPhoto != null) {
                coverURL = coverPhoto.getUrl();
            }
        }
        //Create a new User with empty groups, contacts, and channels
        return User.Companion.create(userId, firstName, lastName, email, profileURL, coverURL,
            null, getDefaultGroups(), null, VERSION_CODE);
    }

    private HashMap<String, Group> getDefaultGroups() {
        HashMap<String, Group> groups = new HashMap<>(3);
        String[] groupLabels = getResources().getStringArray(R.array.default_groups);
        for (String label : groupLabels) {
            Group group = Group.Companion.create(label);
            groups.put(group.id(), group);
        }
        return groups;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _googleApiClient = buildGoogleApiClient(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (_googleApiClient.isConnected()) {
            _googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onActivityResult(
        int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                onGooglePlusSignIn(result.getSignInAccount());
            } else {
                onGooglePlusError(result.getStatus());
            }
        }
    }
}
