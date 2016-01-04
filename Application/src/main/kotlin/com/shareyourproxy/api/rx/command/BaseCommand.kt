package com.shareyourproxy.api.rx.command

import android.os.Parcelable

/**
 * Send a command request to be processed in ProxyApplication.
 */
internal abstract class BaseCommand : ExecuteCommand, Parcelable {
    override fun describeContents(): Int = 0
}
