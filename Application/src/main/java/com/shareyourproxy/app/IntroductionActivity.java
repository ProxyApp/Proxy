package com.shareyourproxy.app;

import android.os.Bundle;

import com.firebase.client.AuthData;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.shareyourproxy.R;
import com.shareyourproxy.app.fragment.MainIntroductionFragment;

import timber.log.Timber;

/**
 * Created by Evan on 9/21/15.
 */
public class IntroductionActivity extends GoogleApiActivity {

    private GoogleApiClient _googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction);
        _googleApiClient = getGoogleApiClient();
        if (savedInstanceState == null) {
            MainIntroductionFragment mainFragment = MainIntroductionFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_introduction_container, mainFragment)
                .commit();
        }
    }

    @Override
    public void onAuthenticated(AuthData authData) {
    }

    @Override
    public void onAuthenticationError(Throwable e) {
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
}
