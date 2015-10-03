package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.gson.AutoValueClass;

import java.util.HashSet;
import java.util.UUID;

import auto.parcel.AutoParcel;

/**
 * Groups are collections of {@link User}s.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_Group.class)
public abstract class Group implements Parcelable {
    public static final String PUBLIC = "public";
    public static final String BLANK = "";

    /**
     * create a new blank Group.
     *
     * @return Immutable group
     */
    @SuppressWarnings("unused")
    public static Group createBlank() {
        String groupId = UUID.randomUUID().toString();
        return builder().id(groupId).label(BLANK).channels(null)
            .contacts(null).build();
    }

    public static Group create(String label) {
        String groupId = UUID.randomUUID().toString();
        return builder().id(groupId).label(label).channels(null)
            .contacts(null).build();
    }

    public static Group createPublicGroup() {
        return builder().id(PUBLIC).label(PUBLIC).channels(null)
            .contacts(null).build();
    }

    public static Group copy(Group group, String newTitle, HashSet<String> channels) {
        return builder().id(group.id()).label(newTitle).channels(channels)
            .contacts(group.contacts()).build();
    }

    public static Group copy(
        String id, String label, HashSet<String> channels, HashSet<String> contacts) {
        return builder().id(id).label(label).channels(channels)
            .contacts(contacts).build();
    }

    /**
     * Group builder.
     *
     * @return this Group.
     */
    public static Builder builder() {
        return new AutoParcel_Group.Builder();
    }

    /**
     * Get the ID of the {@link Group}
     *
     * @return name
     */
    public abstract String id();

    /**
     * Get the name of the {@link Group}.
     *
     * @return name
     */
    public abstract String label();

    /**
     * Get the list of {@link Channel}s tied to this {@link Group}.
     *
     * @return list of {@link Channel}s
     */
    @Nullable
    public abstract HashSet<String> channels();

    /**
     * Get the list of {@link Contact}s in this {@link Group}.
     *
     * @return list of {@link Contact}s
     */
    @Nullable
    public abstract HashSet<String> contacts();

    /**
     * Group Builder.
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
         * Set the contactGroups name.
         *
         * @param label group name
         * @return label
         */
        Builder label(String label);

        /**
         * Set group channels.
         *
         * @param channels group channels
         * @return channels
         */
        @Nullable
        Builder channels(HashSet<String> channels);

        /**
         * Set group contacts.
         *
         * @param contacts this contactGroups contacts
         * @return contacts
         */
        @Nullable
        Builder contacts(HashSet<String> contacts);

        /**
         * BUILD.
         *
         * @return Group
         */
        Group build();
    }


}
