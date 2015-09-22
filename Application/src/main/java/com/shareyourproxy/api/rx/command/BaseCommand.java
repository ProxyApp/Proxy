package com.shareyourproxy.api.rx.command;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.api.rx.RxBusDriver;

/**
 * Send a command request to be processed in ProxyApplication.
 */
public abstract class BaseCommand implements ExecuteCommand, Parcelable {
    public final ComponentName componentName;
    public final RxBusDriver rxBus;

    protected BaseCommand(
        String packageName, String className, RxBusDriver rxBus) {
        // its already parcelable, thanks android!
        this.componentName = new ComponentName(packageName, className);
        this.rxBus = rxBus;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(rxBus);
    }
}
