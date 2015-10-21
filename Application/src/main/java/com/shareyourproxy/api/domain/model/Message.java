package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.domain.factory.AutoValueClass;

import auto.parcel.AutoParcel;

/**
 * Message information for Notifications.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_Message.class)
public abstract class Message implements Parcelable {

    public static Message create(String id, String contactId, String first, String last) {
        return builder().id(id).contactId(contactId).firstName(first).lastName(last).build();
    }

    /**
     * Message builder.
     *
     * @return this Message.
     */
    public static Builder builder() {
        return new AutoParcel_Message.Builder();
    }

    /**
     * Get the ID of the message.
     *
     * @return id of message
     */
    public abstract String id();

    /**
     * If you're receiving this message, you are the contactId that a user added.
     *
     * @return contactId the user added
     */
    public abstract String contactId();

    /**
     * Contact first name.
     *
     * @return name
     */
    public abstract String firstName();

    /**
     * contact last name
     *
     * @return name
     */
    public abstract String lastName();

    /**
     * Message Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the contactGroups Id.
         *
         * @param id group unique groupId
         * @return group groupId
         */
        Builder id(String id);

        /**
         * Set the contactId
         *
         * @param contactId added to user
         * @return contactId
         */
        Builder contactId(String contactId);

        /**
         * Set the contact first name
         *
         * @param firstName of user
         * @return contact first name
         */
        Builder firstName(String firstName);

        /**
         * Set the contact last name
         *
         * @param lastName of user
         * @return contact last name
         */
        Builder lastName(String lastName);

        /**
         * BUILD.
         *
         * @return Message
         */
        Message build();
    }
}
