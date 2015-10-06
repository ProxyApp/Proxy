package com.shareyourproxy.api.domain.model;

import android.os.Parcelable;
import android.support.annotation.Nullable;

import com.shareyourproxy.api.domain.factory.AutoValueClass;

import java.util.Date;

import auto.parcel.AutoParcel;

/**
 * Make an item for user activity feeds.
 */
@AutoParcel
@AutoValueClass(autoValueClass = AutoParcel_ActivityFeedItem.class)
public abstract class ActivityFeedItem implements Parcelable {

    /**
     * Create a new {@link Channel}.
     *
     * @param subtext       subtext content
     * @param channelType   channel type for image
     * @param actionAddress intent endpoint
     * @param timestamp     when it happened
     * @return Immutable ActivityFeedItem
     */
    @SuppressWarnings("unused")
    public static ActivityFeedItem create(
        String handle, String subtext, ChannelType channelType, String actionAddress, Date timestamp) {
        return builder().handle(handle).subtext(subtext).channelType(channelType)
            .actionAddress(actionAddress).timestamp(timestamp).isError(false).build();
    }

    public static ActivityFeedItem createEmpty(ChannelType channelType) {
        return builder().handle("").subtext("").channelType(channelType)
            .actionAddress("").timestamp(null).isError(true).build();
    }

    /**
     * User builder.
     *
     * @return this User.
     */
    private static Builder builder() {
        return new AutoParcel_ActivityFeedItem.Builder();
    }

    /**
     * Get the channel handle.
     *
     * @return handle
     */
    @Nullable
    public abstract String handle();

    /**
     * Get the subtext.
     *
     * @return subtext
     */
    @Nullable
    public abstract String subtext();

    /**
     * Channel image resource.
     *
     * @return image resource
     */
    public abstract ChannelType channelType();

    /**
     * Intent action address.
     *
     * @return intent endpoint
     */
    @Nullable
    public abstract String actionAddress();

    /**
     * When did the feed item occur.
     *
     * @return date string
     */
    @Nullable
    public abstract Date timestamp();

    public abstract boolean isError();

    /**
     * ActivityFeedItem Builder.
     */
    @AutoParcel.Builder
    public interface Builder {

        /**
         * Set the feed items channel handle
         *
         * @param handle content
         * @return subtext
         */
        Builder handle(String handle);

        /**
         * Set the feed items subtext content
         *
         * @param subtext content
         * @return subtext
         */
        Builder subtext(String subtext);

        /**
         * ChannelType
         *
         * @param channelType for image
         * @return channelType
         */
        Builder channelType(ChannelType channelType);

        /**
         * Action Address to handle linking
         *
         * @param actionAddress intent address
         * @return action address
         */
        Builder actionAddress(String actionAddress);

        /**
         * Time stamp for content
         *
         * @param timestamp date
         * @return time stamp
         */
        Builder timestamp(Date timestamp);

        /**
         * Time stamp for content
         *
         * @param timestamp date
         * @return time stamp
         */
        Builder isError(boolean isError);

        /**
         * BUILD.
         *
         * @return new activity feed item
         */
        ActivityFeedItem build();
    }

}
