package com.proxy.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proxy.R;
import com.proxy.api.gson.AutoParcelAdapterFactory;
import com.proxy.api.service.UserService;

import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Rest client for users.
 */
public class RestClient {
    private UserService userService;
    private RestAdapter mRestAdapter;

    /**
     * Singleton.
     *
     * @param context app context
     * @return RestClient
     */
    public static RestClient newInstance(Context context) {
        return new RestClient(context);
    }

    /**
     * Constructor.
     *
     * @param context app context
     */
    public RestClient(Context context) {
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'")
            .registerTypeAdapterFactory(new AutoParcelAdapterFactory())
            .create();

        mRestAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(context.getResources().getString(R.string.firebase_url))
            .setConverter(new GsonConverter(gson))
            .build();
    }

    /**
     * Get the user Service.
     *
     * @return userService
     */
    public UserService getUserService() {
        if (userService == null) {
            userService = mRestAdapter.create(UserService.class);
        }
        return userService;
    }
}
