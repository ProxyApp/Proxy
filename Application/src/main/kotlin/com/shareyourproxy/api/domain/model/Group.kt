package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable
import java.util.*

/**
 * Groups are collections of [User]s.
 */
internal data class Group(val id: String, val label: String, val channels: HashSet<String>, val contacts: HashSet<String>) : BaseParcelable {
    constructor() : this("", "", HashSet(0), HashSet(0))

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(label)
        dest.writeValue(channels)
        dest.writeValue(contacts)
    }

    companion object {
        val CL = Group::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<Group> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<Group?> = arrayOfNulls(size)
        }
        @Suppress("UNCHECKED_CAST")
        private fun readParcel(parcel: Parcel) = Group(parcel.readString(), parcel.readString(), parcel.readValue(CL) as HashSet<String>, parcel.readValue(CL) as HashSet<String>)
    }
}
