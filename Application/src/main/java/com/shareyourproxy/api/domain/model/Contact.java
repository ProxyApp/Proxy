package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.gson.AutoGson;

import java.util.ArrayList;

import auto.parcel.AutoParcel;

/**
 * Contacts are {@link User}s that you'd like to communicate with.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Contact.class)
public abstract class Contact implements Parcelable {

    /**
     * Create a new Contact.
     *
     * @param id       unique identifier
     * @param channels list of contacts channels
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Contact create(Id id, String first, String last,String imageURL, ArrayList<Channel> channels) {
        return builder().id(id).first(first).last(last).imageURL(imageURL).channels(channels).build();
    }

    /**
     * Contact builder.
     *
     * @return this Contact.
     */
    public static Builder builder() {
        return new AutoParcel_Contact.Builder();
    }

    /**
     * Get the ID of the Contact.
     *
     * @return name
     */
    public abstract Id id();

    /**
     * Get the name of the Contact.
     *
     * @return name
     */
    public abstract String first();

    /**
     * Get the name of the Contact.
     *
     * @return name
     */
    public abstract String last();

    /**
     * Get group profile image.
     *
     * @return profile image
     */
    public abstract String imageURL();

    /**
     * Get the list of channels a group has.
     *
     * @return list of group's channels
     */
    @Nullable
    public abstract ArrayList<Channel> channels();

    /**
     * Group Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the contacts Id.
         *
         * @param id group unique id
         * @return group id
         */
        Builder id(Id id);

        /**
         * Set the contacts first name.
         *
         * @param first group's first name
         * @return label
         */
        Builder first(String first);

        /**
         * Set the contacts last name.
         *
         * @param last group's last name
         * @return label
         */
        Builder last(String last);

        /**
         * Set the contacts name.
         *
         * @param label group name
         * @return label
         */
        Builder imageURL(String imageURL);

        /**
         * Set group channels.
         *
         * @param channels group channels
         * @return channels
         */
        @Nullable
        Builder channels(ArrayList<Channel> channels);

        /**
         * BUILD.
         *
         * @return Contact
         */
        Contact build();
    }

}
