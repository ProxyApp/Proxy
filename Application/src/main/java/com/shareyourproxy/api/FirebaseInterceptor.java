package com.shareyourproxy.api;

import android.content.SharedPreferences;

import com.shareyourproxy.Constants;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;

import timber.log.Timber;

import static com.shareyourproxy.Constants.QUERY_AUTH;

/**
 * Created by Evan on 9/17/15.
 */
public class FirebaseInterceptor implements Interceptor {

    private final SharedPreferences _sharedPrefs;

    public FirebaseInterceptor(SharedPreferences sharedPrefs) {
        _sharedPrefs = sharedPrefs;
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = _sharedPrefs.getString(Constants.KEY_GOOGLE_PLUS_AUTH, null);
        Request request = chain.request();
        if (token != null) {
            HttpUrl url = request.httpUrl().newBuilder()
                .addQueryParameter(QUERY_AUTH, token)
                .build();
            request = chain.request().newBuilder().url(url).build();
        }
        Response res = chain.proceed(request);
        Timber.i("Retrofit Response: %1$s", res.toString());
        return res;
    }
}
