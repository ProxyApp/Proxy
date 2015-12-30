package com.shareyourproxy.util

import android.os.Parcelable

/**
 * Describe those contents.
 */
interface BaseParcelable : Parcelable {
    override fun describeContents() = 0
}