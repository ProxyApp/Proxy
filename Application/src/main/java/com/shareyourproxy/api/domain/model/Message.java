package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Message information for Notifications.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Message.class)
public abstract class Message implements Parcelable {

    public static Message create(Id id, Contact contact) {
        return builder().id(id).contact(contact).build();
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
    public abstract Id id();

    /**
     * If you're receiving this message, you are the contact that a user added.
     *
     * @return contact the user added
     */
    @Nullable
    public abstract Contact contact();

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
        Builder id(Id id);

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
         * @return Message
         */
        Message build();
    }
}
