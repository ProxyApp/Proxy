package com.proxy.api.rx;

import android.util.Log;

import rx.Observer;
import timber.log.Timber;

/**
 * Created by Evan on 5/21/15.
 */
public abstract class EndObserver<T> implements Observer<T> {

    @Override
    public void onError(Throwable e) {
        Timber.e(Log.getStackTraceString(e));
        this.onError();
    }

    public abstract void onError();
}
