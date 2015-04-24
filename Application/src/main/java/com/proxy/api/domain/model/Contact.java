package com.proxy.api.domain.model;

import android.support.annotation.Nullable;

import com.proxy.api.gson.AutoGson;

import java.util.ArrayList;

import auto.parcel.AutoParcel;

/**
 * Contacts are {@link User}s that you'd like to communicate with.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Contact.class)
public abstract class Contact {

    /**
     * Create a new Contact.
     *
     * @param id       unique identifier
     * @param name     name of the contact
     * @param channels list of contacts channels
     * @return Immutable contact
     */
    @SuppressWarnings("unused")
    public static Contact create(String id, String label, ArrayList<Channel> channels) {
        return new AutoParcel_Contact(id, label, channels);
    }

    /**
     * Get the ID of the Contact.
     *
     * @return name
     */
    public abstract String contactId();

    /**
     * Get the name of the Contact.
     *
     * @return name
     */
    public abstract String label();

    /**
     * Get the list of channels a contact has.
     *
     * @return list of contact's channels
     */
    @Nullable
    public abstract ArrayList<Channel> channels();

}
