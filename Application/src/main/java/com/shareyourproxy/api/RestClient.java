package com.shareyourproxy.api;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.shareyourproxy.BuildConfig;
import com.shareyourproxy.Constants;
import com.shareyourproxy.api.gson.AutoValueAdapterFactory;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.service.InstagramAuthService;
import com.shareyourproxy.api.service.MessageService;
import com.shareyourproxy.api.service.SharedLinkService;
import com.shareyourproxy.api.service.SpotifyAuthService;
import com.shareyourproxy.api.service.SpotifyUserService;
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

    //    public static final String INSTAGRAM_URL = "https://api.instagram.com/v1";
    public static final String SPOTIFY_URL = "https://api.spotify.com/v1";
    public static final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/api";
    public static final String INSTAGRAM_AUTH_URL = "https://api.instagram.com/oauth/";

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
    public static UserService getUserService(Context context, RxBusDriver rxBus) {
        return buildRestClient(context, rxBus, buildGsonConverter())
            .create(UserService.class);
    }

    public static UserGroupService getUserGroupService(Context context, RxBusDriver rxBus) {
        return buildRestClient(context, rxBus, buildGsonConverter())
            .create(UserGroupService.class);
    }

    public static UserChannelService getUserChannelService(Context context, RxBusDriver rxBus) {
        return buildRestClient(context, rxBus, buildGsonConverter())
            .create(UserChannelService.class);
    }

    public static UserContactService getUserContactService(Context context, RxBusDriver rxBus) {
        return buildRestClient(context, rxBus, buildGsonConverter())
            .create(UserContactService.class);
    }

    public static MessageService getMessageService(Context context, RxBusDriver rxBus) {
        return buildRestClient(context, rxBus, buildGsonConverter())
            .create(MessageService.class);
    }

    public static SharedLinkService getSharedLinkService(Context context, RxBusDriver rxBus) {
        return buildRestClient(context, rxBus, buildGsonConverter())
            .create(SharedLinkService.class);
    }

    public static InstagramAuthService getInstagramAuthService(Context context, RxBusDriver rxBus) {
        return buildInstagramAuthClient(context, rxBus, buildGsonConverter())
            .create(InstagramAuthService.class);
    }

    public static SpotifyUserService getSpotifyUserService(Context context, RxBusDriver rxBus) {
        return buildSpotifyUserClient(context, rxBus, buildGsonConverter())
            .create(SpotifyUserService.class);
    }

    public static SpotifyAuthService getSpotifyAuthService(Context context, RxBusDriver rxBus) {
        return buildSpotifyAuthClient(context, rxBus, buildGsonConverter())
            .create(SpotifyAuthService.class);
    }

    public static RestAdapter buildRestClient(
        Context context, RxBusDriver rxBus, Gson gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(BuildConfig.FIREBASE_ENDPOINT)
            .setConverter(new GsonConverter(gson))
            .setRequestInterceptor(new FirebaseInterceptor(sharedPrefs))
            .build();
    }

    public static RestAdapter buildInstagramAuthClient(Context context, RxBusDriver rxBus, Gson
        gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(INSTAGRAM_AUTH_URL)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static RestAdapter buildSpotifyUserClient(Context context, RxBusDriver rxBus, Gson
        gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(SPOTIFY_URL)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static RestAdapter buildSpotifyAuthClient(Context context, RxBusDriver rxBus, Gson
        gson) {
        SharedPreferences sharedPrefs =
            context.getSharedPreferences(Constants.MASTER_KEY, Context.MODE_PRIVATE);
        return new RestAdapter.Builder()
            .setLogLevel(RestAdapter.LogLevel.FULL)
            .setClient(new OkClient(getClient(rxBus, sharedPrefs)))
            .setEndpoint(SPOTIFY_AUTH_URL)
            .setConverter(new GsonConverter(gson))
            .build();
    }

    public static Gson buildGsonConverter() {
        return new GsonBuilder()
            .registerTypeAdapterFactory(new AutoValueAdapterFactory())
            .create();
    }

    public static OkHttpClient getClient(RxBusDriver rxBus, SharedPreferences sharedPrefs) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(10, TimeUnit.SECONDS);
        client.setReadTimeout(10, TimeUnit.SECONDS);
        client.setAuthenticator(new FirebaseAuthenticator(rxBus, sharedPrefs));
        return client;
    }
}
