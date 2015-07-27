package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.gson.AutoGson;

import java.util.UUID;

import auto.parcel.AutoParcel;

/**
 * Upload information to firebase to create shared links for group channels.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_SharedLink.class)
public abstract class SharedLink implements Parcelable {

    /**
     * Create a new Contact.
     *
     * @param user of shared content
     * @param group to share
     * @return shared link
     */
    @SuppressWarnings("unused")
    public static SharedLink create(User user, Group group) {
        String id = UUID.randomUUID().toString();
        return builder().id(id).userId(user.id().value()).groupId(group.id().value()).build();
    }

    /**
     * Contact builder.
     *
     * @return this Contact.
     */
    public static Builder builder() {
        return new AutoParcel_SharedLink.Builder();
    }

    /**
     * Group Id.
     *
     * @return name
     */
    public abstract String id();

    /**
     * Group to share.
     *
     * @return name
     */
    public abstract String groupId();

    /**
     * Shared Group's User.
     *
     * @return name
     */
    public abstract String userId();

    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the Id.
         *
         * @param id group id
         * @return builder
         */
        Builder id(String id);

        /**
         * Set the group.
         *
         * @param group group object
         * @return builder
         */
        Builder groupId(String group);

        /**
         * Set the shared links user.
         *
         * @param user of shared group
         * @return builder
         */
        Builder userId(String user);

        /**
         * BUILD.
         *
         * @return SharedLink
         */
        SharedLink build();
    }
}
