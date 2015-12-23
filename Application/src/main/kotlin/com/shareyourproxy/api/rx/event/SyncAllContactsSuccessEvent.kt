package com.shareyourproxy.api.rx.event

import android.os.Parcel
import android.os.Parcelable

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Sync all users command has successfully completed.
 */
class SyncAllContactsSuccessEvent : EventCallback() {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
    }

    companion object {
        val CREATOR: Parcelable.Creator<SyncAllContactsSuccessEvent> = object : Parcelable.Creator<SyncAllContactsSuccessEvent> {
            override fun createFromParcel(parcel: Parcel): SyncAllContactsSuccessEvent {
                return SyncAllContactsSuccessEvent()
            }

            override fun newArray(size: Int): Array<SyncAllContactsSuccessEvent?> {
                return arrayOfNulls(size)
            }
        }
    }
}
