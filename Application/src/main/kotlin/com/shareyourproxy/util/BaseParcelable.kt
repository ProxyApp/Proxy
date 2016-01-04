package com.shareyourproxy.util

import android.os.Parcelable

/**
 * Describe those contents.
 */
internal interface BaseParcelable : Parcelable {
    override fun describeContents() = 0
}