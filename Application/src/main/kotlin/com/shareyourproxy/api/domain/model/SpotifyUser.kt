package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable

/**
 * Created by Evan on 8/14/15.
 */
data class SpotifyUser(val id: String,val name: String,val uri: String) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(name)
        dest.writeString(uri)
    }
    companion object {
        val CREATOR = object : Parcelable.Creator<SpotifyUser> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<SpotifyUser?> = arrayOfNulls(size)
        }
        private fun readParcel(parcel: Parcel) = SpotifyUser(parcel.readString(), parcel.readString(), parcel.readString())
    }
}
