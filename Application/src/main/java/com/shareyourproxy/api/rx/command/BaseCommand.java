package com.shareyourproxy.api.rx.command;

import android.content.ComponentName;
import android.os.Parcelable;

/**
 * Send a command request to be processed in ProxyApplication.
 */
public abstract class BaseCommand implements ExecuteCommand, Parcelable {
    public final ComponentName componentName;

    protected BaseCommand(String packageName, String className) {
        // its already parcelable, thanks android!
        this.componentName = new ComponentName(packageName, className);
    }
}
