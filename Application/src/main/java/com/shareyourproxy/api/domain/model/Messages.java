package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;

import com.shareyourproxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Messages information for Notifications.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Messages.class)
public abstract class Messages implements Parcelable {

    public static Messages create(Id id, User user, Contact contact) {
        return builder().id(id).user(user).contact(contact).build();
    }

    /**
     * Messages builder.
     *
     * @return this Messages.
     */
    public static Builder builder() {
        return new AutoParcel_Messages.Builder();
    }

    /**
     * Get the ID of the message.
     *
     * @return id of message
     */
    public abstract Id id();

    /**
     * The User that Added the messages recipient as a contact
     *
     * @return user adding a contact
     */
    public abstract User user();

    /**
     * If you're receiving this message, you are the contact that a user added.
     *
     * @return contact the user added
     */
    public abstract Contact contact();

    /**
     * Messages Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the contactGroups Id.
         *
         * @param id group unique groupId
         * @return group groupId
         */
        Builder id(Id id);

        /**
         * Set the User.
         *
         * @param user adding contact
         * @return user
         */
        Builder user(User user);

        /**
         * Set the contact.
         *
         * @param contact added to user
         * @return contact
         */
        Builder contact(Contact contact);

        /**
         * BUILD.
         *
         * @return Messages
         */
        Messages build();
    }
}
