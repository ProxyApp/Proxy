package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable
import java.util.*

/**
 * Groups are collections of [User]s.
 */
data class Group(val id: String, val label: String, val channels: HashSet<String>, val contacts: HashSet<String>) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(label)
        dest.writeSerializable(channels)
    }

    companion object {
        val CL = Group::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<Group> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<Group?> = arrayOfNulls(size)
        }

        @Suppress("UNCHECKED_CAST")
        private fun readParcel(parcel: Parcel) = Group(parcel.readString(), parcel.readString(), parcel.readSerializable() as HashSet<String>,parcel.readSerializable() as HashSet<String>)
    }
}
