package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable

/**
 * Instagram User Object.
 */
internal data class InstagramUser(val id: String, val username: String, val fullName: String, val profilePicture: String, val bio: String, val website: String) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(username)
        dest.writeString(fullName)
        dest.writeString(profilePicture)
        dest.writeString(bio)
        dest.writeString(website)
    }

    companion object {
        @JvmField val CREATOR = object : Parcelable.Creator<InstagramUser> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<InstagramUser?> = arrayOfNulls(size)
        }

        private fun readParcel(parcel: Parcel) = InstagramUser(parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString(), parcel.readString())
    }
}
