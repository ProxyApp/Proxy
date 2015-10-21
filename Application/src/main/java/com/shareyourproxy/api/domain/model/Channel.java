package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.domain.factory.AutoValueClass;

import auto.parcel.AutoParcel;

/**
 * Channels are other apps and services that you will use to communicate with {@link Contact}s.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_Channel.class)
public abstract class Channel implements Parcelable {

    /**
     * Create a new {@link Channel}.
     *
     * @param id            unique id
     * @param label         name of the newChannel
     * @param channelType   newChannel intent type
     * @param actionAddress endpoint
     * @param isPublic      is channel public
     * @return Immutable Channel
     */
    @SuppressWarnings("unused")
    public static Channel create(
        String id, String label, ChannelType channelType, String
        actionAddress, Boolean isPublic) {
        return builder().id(id).label(label).channelType(channelType)
            .actionAddress(actionAddress).isPublic(isPublic).build();
    }

    /**
     * User builder.
     *
     * @return this User.
     */
    public static Builder builder() {
        return new AutoParcel_Channel.Builder();
    }

    /**
     * Get the ID of the {@link Channel}.
     *
     * @return name
     */
    public abstract String id();

    /**
     * Get the name of the {@link Channel}.
     *
     * @return newChannel label
     */
    public abstract String label();

    /**
     * Channel image resource.
     *
     * @return image resource
     */
    public abstract ChannelType channelType();

    /**
     * Channel image resource.
     *
     * @return image resource
     */
    public abstract String actionAddress();

    /**
     * Is this channel public to all contacts?
     */
    @Nullable
    public abstract Boolean isPublic();

    /**
     * Channel Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the newChannel Id.
         *
         * @param id newChannel unique id
         * @return newChannel id
         */
        Builder id(String id);

        /**
         * Set the channels name.
         *
         * @param label newChannel name
         * @return label
         */
        Builder label(String label);

        Builder channelType(ChannelType channelType);

        Builder actionAddress(String actionAddress);

        Builder isPublic(Boolean isPublic);

        /**
         * BUILD.
         *
         * @return newChannel
         */
        Channel build();
    }

}