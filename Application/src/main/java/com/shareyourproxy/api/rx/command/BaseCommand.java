package com.shareyourproxy.api.rx.command;

import android.os.Parcelable;

/**
 * Send a command request to be processed in ProxyApplication.
 */
public abstract class BaseCommand implements ExecuteCommand, Parcelable {
}
