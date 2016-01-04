package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable
import java.util.*

/**
 * Upload information to firebase to create shared links for group channels.
 */
internal data class SharedLink(val id: String, val userId: String, val groupId: String) : BaseParcelable {
    public constructor(userId: String, groupId: String) : this(UUID.randomUUID().toString(), groupId, userId)

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(userId)
        dest.writeString(groupId)
    }

    companion object {
        val CREATOR = object : Parcelable.Creator<SharedLink> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<SharedLink?> = arrayOfNulls(size)
        }

        private fun readParcel(parcel: Parcel) = SharedLink(parcel.readString(), parcel.readString(), parcel.readString())
    }
}
