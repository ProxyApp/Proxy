package com.shareyourproxy.api.rx;

import android.util.Log;

import rx.SingleSubscriber;
import timber.log.Timber;

/**
 * Single subscriber that logs.
 */
public class JustSingle<T> extends SingleSubscriber<T> {
    @Override
    public void onSuccess(T value) {
        Timber.v("%1$s Success obj: %2$s", this.toString(), value.toString());
    }

    @Override
    public void onError(Throwable e) {
        Timber.e(Log.getStackTraceString(e));
    }
}
