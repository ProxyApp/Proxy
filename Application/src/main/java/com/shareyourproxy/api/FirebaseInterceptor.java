package com.shareyourproxy.api;

import android.content.SharedPreferences;

import com.shareyourproxy.Constants;

import retrofit.RequestInterceptor;

import static com.shareyourproxy.Constants.QUERY_AUTH;

/**
 * Created by Evan on 9/17/15.
 */
public class FirebaseInterceptor implements RequestInterceptor {

    private final SharedPreferences _sharedPrefs;

    public FirebaseInterceptor(SharedPreferences sharedPrefs) {
        _sharedPrefs = sharedPrefs;
    }

    @Override
    public void intercept(RequestFacade request) {
        String token = _sharedPrefs.getString(Constants.KEY_GOOGLE_PLUS_AUTH, null);
        if (token != null) {
            request.addQueryParam(QUERY_AUTH, token);
        }
    }
}
