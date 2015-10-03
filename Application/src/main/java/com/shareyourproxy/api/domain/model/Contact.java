package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.gson.AutoValueClass;

import java.util.HashMap;

import auto.parcel.AutoParcel;

/**
 * Contacts are {@link User}s that you'd like to communicate with.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_Contact.class)
public abstract class Contact implements Parcelable {

    /**
     * Create a new Contact.
     *
     * @param id       unique identifier
     * @param channels list of contacts channels
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Contact create(
        String id, String first, String last, String profileURL, String coverURL,
        HashMap<String, Channel> channels) {
        return builder().id(id).first(first).last(last).profileURL(profileURL).coverURL(coverURL)
            .channels(channels).build();
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
    public abstract String id();

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
     * Get contactId profile image.
     *
     * @return profile image
     */
    public abstract String profileURL();

    /**
     * Get contactId cover image.
     *
     * @return profile image
     */
    @Nullable
    public abstract String coverURL();

    /**
     * Get the list of channels a contactId has.
     *
     * @return list of group's channels
     */
    @Nullable
    public abstract HashMap<String, Channel> channels();

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
        Builder id(String id);

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
         * @param profileURL profile image
         * @return label
         */
        Builder profileURL(String profileURL);

        /**
         * Set the contacts name.
         *
         * @param coverURL cover image
         * @return label
         */
        @Nullable
        Builder coverURL(String coverURL);

        /**
         * Set group channels.
         *
         * @param channels contactId channels
         * @return channels
         */
        @Nullable
        Builder channels(HashMap<String, Channel> channels);

        /**
         * BUILD.
         *
         * @return Contact
         */
        Contact build();
    }

}
