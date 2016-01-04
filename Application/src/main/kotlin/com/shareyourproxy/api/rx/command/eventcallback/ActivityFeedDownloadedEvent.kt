package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.domain.model.ActivityFeedItem

/**
 * The activities feed has been downloaded.
 */
internal final class ActivityFeedDownloadedEvent(val feedItems: List<ActivityFeedItem>) : EventCallback() {

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(feedItems)
    }

    companion object {
        private val CL = ActivityFeedDownloadedEvent::class.java.classLoader
        val CREATOR: Parcelable.Creator<ActivityFeedDownloadedEvent> = object : Parcelable.Creator<ActivityFeedDownloadedEvent> {
            @Suppress("UNCHECKED_CAST")
            override fun createFromParcel(parcel: Parcel): ActivityFeedDownloadedEvent {
                return ActivityFeedDownloadedEvent(parcel.readValue(CL) as List<ActivityFeedItem>)
            }

            override fun newArray(size: Int): Array<ActivityFeedDownloadedEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
