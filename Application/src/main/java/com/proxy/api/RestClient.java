package com.proxy.api;

import android.content.Context;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.proxy.R;
import com.proxy.api.service.UserService;

import io.realm.RealmObject;
import retrofit.RestAdapter;
import retrofit.converter.GsonConverter;

/**
 * Rest client for users.
 */
public class RestClient {
    private UserService userService;
    private RestAdapter mRestAdapter;

    /**
     * Constructor.
     *
     * @param context app context
     */
    public RestClient(Context context) {
        Gson gson = new GsonBuilder()
            .setExclusionStrategies(new ExclusionStrategy() {
                @Override
                public boolean shouldSkipField(FieldAttributes f) {
                    return f.getDeclaringClass().equals(RealmObject.class);
                }

                @Override
                public boolean shouldSkipClass(Class<?> clazz) {
                    return false;
                }
            })
            .create();

        mRestAdapter = new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setEndpoint(context.getResources().getString(R.string.firebase_url))
            .setConverter(new GsonConverter(gson))
            .build();
    }

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
     * Get the {@link UserService}.
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
