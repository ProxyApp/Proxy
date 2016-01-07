package com.shareyourproxy.api.rx.event

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Sync all users command has successfully completed.
 */
internal final class SyncContactsSuccessEvent : EventCallback() {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
    }

    companion object {
        val CREATOR: Parcelable.Creator<SyncContactsSuccessEvent> = object : Parcelable.Creator<SyncContactsSuccessEvent> {
            override fun createFromParcel(parcel: Parcel): SyncContactsSuccessEvent {
                return SyncContactsSuccessEvent()
            }

            override fun newArray(size: Int): Array<SyncContactsSuccessEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
