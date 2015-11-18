package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.ActivityFeedItem;

import java.util.List;

/**
 * The activities feed has been downloaded.
 */
public class ActivityFeedDownloadedEvent extends EventCallback {
    private final static java.lang.ClassLoader CL =
        ActivityFeedDownloadedEvent.class.getClassLoader();
    public static final Creator<ActivityFeedDownloadedEvent> CREATOR =
        new Creator<ActivityFeedDownloadedEvent>() {
            @Override
            public ActivityFeedDownloadedEvent createFromParcel(Parcel in) {
                return new ActivityFeedDownloadedEvent(
                    (List<ActivityFeedItem>) in.readValue(CL));
            }

            @Override
            public ActivityFeedDownloadedEvent[] newArray(int size) {
                return new ActivityFeedDownloadedEvent[size];
            }
        };
    public final List<ActivityFeedItem> feedItems;

    public ActivityFeedDownloadedEvent(List<ActivityFeedItem> feedItems) {
        this.feedItems = feedItems;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(feedItems);
    }
}
