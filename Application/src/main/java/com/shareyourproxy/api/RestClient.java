package com.shareyourproxy.api;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.gson.AutoGson;
import com.shareyourproxy.api.gson.AutoParcelAdapterFactory;
import com.shareyourproxy.api.gson.UserTypeAdapter;
import com.shareyourproxy.api.service.GroupContactService;
import com.shareyourproxy.api.service.MessageService;
import com.shareyourproxy.api.service.UserChannelService;
import com.shareyourproxy.api.service.UserContactService;
import com.shareyourproxy.api.service.UserGroupService;
import com.shareyourproxy.api.service.UserService;
import com.squareup.okhttp.OkHttpClient;

import java.util.concurrent.TimeUnit;

import retrofit.RestAdapter;
import retrofit.client.OkClient;
import retrofit.converter.GsonConverter;

/**
 * Rest client for users.
 */
public class RestClient {

    /**
     * Constructor.
     */
    private RestClient() {
    }

    /**
     * Get the {@link UserService}.
     *
     * @return userService
     */
    public static UserService getUserService(final Context context) {
        return buildRestClient(context,
            buildGsonConverter(User.class.getAnnotation(AutoGson.class).autoValueClass(),
                UserTypeAdapter.newInstace()))
            .create(UserService.class);
    }

    public static UserGroupService getUserGroupService(Context context) {
        return buildRestClient(context,
            buildGsonConverter()).create(UserGroupService.class);
    }

    public static UserChannelService getUserChannelService(Context context) {
        return buildRestClient(context,
            buildGsonConverter()).create(UserChannelService.class);
    }

    public static UserContactService getUserContactService(Context context) {
        return buildRestClient(context,
            buildGsonConverter()).create(UserContactService.class);
    }

    public static GroupContactService getGroupContactService(Context context) {
        return buildRestClient(context,
            buildGsonConverter()).create(GroupContactService.class);
    }

    public static MessageService getMessageService(Context context) {
        return buildRestClient(context,
            buildGsonConverter()).create(MessageService.class);
    }

    public static RestAdapter buildRestClient(Context context, Gson gson) {

        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient()))
            .setEndpoint(BuildConfig.FIREBASE_ENDPOINT)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static Gson buildGsonConverter(Class clazz, TypeAdapter typeAdapter) {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new AutoParcelAdapterFactory())
            .registerTypeAdapter(clazz, typeAdapter)
            .create();
    }

    public static Gson buildGsonConverter() {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new AutoParcelAdapterFactory())
            .create();
    }

    public static OkHttpClient getClient() {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(30, TimeUnit.SECONDS);
        client.setReadTimeout(30, TimeUnit.SECONDS);
        return client;
    }
}
