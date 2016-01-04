package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable

/**
 * Message information for Notifications.
 */
internal data class Message(val id: String, val contactId: String, val fullName: String) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(contactId)
        dest.writeString(fullName)
    }

    companion object {
        val CREATOR = object : Parcelable.Creator<Message> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<Message?> = arrayOfNulls(size)
        }

        private fun readParcel(parcel: Parcel) = Message(parcel.readString(), parcel.readString(), parcel.readString())
    }
}
