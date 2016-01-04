package com.shareyourproxy.api.rx.command.eventcallback

import android.os.Parcelable

/**
 * Base event class for all events to extend from.
 */
internal abstract class EventCallback : Parcelable {
    override fun describeContents(): Int = 0
}