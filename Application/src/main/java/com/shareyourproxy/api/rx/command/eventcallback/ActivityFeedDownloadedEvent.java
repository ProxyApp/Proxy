package com.shareyourproxy.api.rx.command.eventcallback;

import android.os.Parcel;

import com.shareyourproxy.api.domain.model.ActivityFeedItem;

import java.util.List;

/**
 * Created by Evan on 10/12/15.
 */
public class ActivityFeedDownloadedEvent extends EventCallback{
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
