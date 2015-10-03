package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.gson.AutoValueClass;

import auto.parcel.AutoParcel;

/**
 * Created by Evan on 8/12/15.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_InstagramAuthResponse.class)
public abstract class InstagramAuthResponse implements Parcelable {

    public static InstagramAuthResponse createBlank(String token, InstagramUser user) {
        return builder().authToken(token).user(user).build();
    }

    public static Builder builder() {
        return new AutoParcel_InstagramAuthResponse.Builder();
    }

    /**
     * Get the ID of the {@link Group}
     *
     * @return name
     */
    public abstract String authToken();

    /**
     * Get the name of the {@link Group}.
     *
     * @return name
     */
    public abstract InstagramUser user();

    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the contactGroups Id.
         *
         * @param token auth token
         * @return token
         */
        Builder authToken(String token);

        /**
         * Set the contactGroups name.
         *
         * @param user instagram user
         * @return user
         */
        Builder user(InstagramUser user);

        /**
         * BUILD.
         *
         * @return Group
         */
        InstagramAuthResponse build();
    }
}
