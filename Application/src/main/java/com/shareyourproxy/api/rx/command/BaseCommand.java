package com.shareyourproxy.api.rx.command;

import android.content.ComponentName;
import android.os.Parcelable;

/**
 * Created by Evan on 6/8/15.
 */
public abstract class BaseCommand implements ExecuteCommand, Parcelable {
    public final ComponentName componentName;

    protected BaseCommand(String packageName, String className) {
        // its already parcelable, thanks android!
        this.componentName = new ComponentName(packageName, className);
    }
}
