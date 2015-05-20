package com.proxy.api.domain.model;

import android.content.Intent;
import android.net.Uri;
import android.os.Parcelable;

import com.proxy.api.gson.AutoGson;

import auto.parcel.AutoParcel;

/**
 * Channels are other apps and services that you will use to communicate with {@link Contact}s.
 */
@AutoParcel
@AutoGson(autoValueClass = AutoParcel_Channel.class)
public abstract class Channel implements Parcelable {

    /**
     * Create a new {@link Channel}.
     *
     * @param id             unique id
     * @param label          name of the channel
     * @param packageName    Name of the channels package
     * @param channelSection section divider category
     * @param channelType    channel intent type
     * @return Immutable channel
     */
    @SuppressWarnings("unused")
    public static Channel create(String id, String label, String packageName,
        ChannelSection channelSection, ChannelType channelType, String actionAddress) {
        return builder().id(id).label(label).packageName(packageName)
            .channelSection(channelSection).channelType(channelType)
            .actionAddress(actionAddress).build();
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
     * @return channel label
     */
    public abstract String label();

    /**
     * Get the name of the {@link Uri} {@link Intent}.
     *
     * @return uri
     */
    public abstract String packageName();

    /**
     * Get the channelSection, or section header for this {@link Channel}.
     *
     * @return header string
     */
    public abstract ChannelSection channelSection();

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
     * Channel Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the channel Id.
         *
         * @param id channel unique id
         * @return channel id
         */
        Builder id(String id);

        /**
         * Set the channels name.
         *
         * @param label channel name
         * @return label
         */
        Builder label(String label);

        Builder packageName(String packageName);

        Builder channelSection(ChannelSection channelSection);

        Builder channelType(ChannelType channelType);

        Builder actionAddress(String actionAddress);

        /**
         * BUILD.
         *
         * @return channel
         */
        Channel build();
    }

}