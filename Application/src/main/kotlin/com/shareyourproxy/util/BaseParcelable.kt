package com.shareyourproxy.util

import android.os.Parcelable

/**
 * Created by Evan on 12/22/15.
 */
interface BaseParcelable : Parcelable {
    override fun describeContents() = 0
}