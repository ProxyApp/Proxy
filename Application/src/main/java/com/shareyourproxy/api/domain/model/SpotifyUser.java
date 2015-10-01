package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.gson.AutoValueClass;

import java.net.URISyntaxException;

import auto.parcel.AutoParcel;

/**
 * Created by Evan on 8/14/15.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_SpotifyUser.class)
public abstract class SpotifyUser implements Parcelable {

    public static SpotifyUser create(
        String id, String name, String uri) {
        return builder().id(id).name(name).uri(uri).build();
    }

    public static Builder builder() {
        return new AutoParcel_SpotifyUser.Builder();
    }

    public static String requestOAuthUrl(
        final String clientId, final String redirectUri) throws URISyntaxException {
        final StringBuilder urlBuilder = new StringBuilder();
        urlBuilder
            .append("https://")
            .append("accounts.spotify.com")
            .append("/en/authorize");
        urlBuilder.append("?response_type=")
            .append("code");
        urlBuilder.append("&client_id=")
            .append(clientId);
        urlBuilder.append("&redirect_uri=")
            .append(redirectUri);
        urlBuilder.append("&scope=")
            .append("user-read-private")
            .append("%20user-read-email")
            .append("%20user-read-birthdate");
        return urlBuilder.toString();
    }

    public abstract String id();

    public abstract String name();

    public abstract String uri();

    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set user id.
         *
         * @param id user unique id
         * @return user id
         */
        Builder id(String id);

        /**
         * Set user first name.
         *
         * @param name user's full name
         * @return user's name
         */
        Builder name(String name);

        /**
         * Set users full name
         *
         * @param uri uri to open spotify
         * @return uri string
         */
        Builder uri(String uri);

        /**
         * BUILD.
         *
         * @return InstagramUser
         */
        SpotifyUser build();
    }
}
