package com.shareyourproxy.api.domain.model

import android.os.Parcel
import android.os.Parcelable
import com.shareyourproxy.util.BaseParcelable

/**
 * Model for [UserGroupsDialog].
 */
internal data class GroupToggle(var group: Group, var isChecked: Boolean) : BaseParcelable {
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeValue(group)
        dest.writeValue(isChecked)
    }

    companion object {
        private val CL = GroupToggle::class.java.classLoader
        val CREATOR = object : Parcelable.Creator<GroupToggle> {
            override fun createFromParcel(parcel: Parcel) = readParcel(parcel)
            override fun newArray(size: Int): Array<GroupToggle?> = arrayOfNulls(size)
        }

        private fun readParcel(parcel: Parcel) = GroupToggle(parcel.readValue(CL) as Group, parcel.readValue(CL) as Boolean)
    }
}
