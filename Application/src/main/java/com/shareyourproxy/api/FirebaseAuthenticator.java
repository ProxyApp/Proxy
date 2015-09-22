package com.shareyourproxy.api;

import android.content.SharedPreferences;

import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.event.RefreshFirebaseAuthenticationEvent;
import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.net.Proxy;

import static com.shareyourproxy.Constants.KEY_GOOGLE_PLUS_AUTH;
import static com.shareyourproxy.Constants.QUERY_AUTH;

/**
 * Created by Evan on 9/16/15.
 */
public class FirebaseAuthenticator implements Authenticator {
    private final RxBusDriver _rxBus;
    private final SharedPreferences _sharedPrefs;

    public FirebaseAuthenticator(RxBusDriver rxBus, SharedPreferences sharedPrefs) {
        _rxBus = rxBus;
        _sharedPrefs = sharedPrefs;
    }

    @Override
    public Request authenticate(Proxy proxy, Response response) throws IOException {
        String token = _sharedPrefs.getString(KEY_GOOGLE_PLUS_AUTH, null);
        Request request = response.request();
        _rxBus.post(new RefreshFirebaseAuthenticationEvent());
        if (token != null) {
            HttpUrl httpUrl = request.httpUrl();
            HttpUrl url = httpUrl.newBuilder().addQueryParameter(QUERY_AUTH, token).build();
            request = response.request().newBuilder().url(url).build();
        }
        return request;
    }

    @Override
    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
        _rxBus.post(new RefreshFirebaseAuthenticationEvent());
        return response.request();
    }
}
