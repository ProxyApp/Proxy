package com.shareyourproxy.api.rx;

import android.util.Log;

import rx.Observer;
import timber.log.Timber;

/**
 * Created by Evan on 5/15/15.
 */
public abstract class JustObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {
        Timber.e(Log.getStackTraceString(e));
        this.onError();
    }

    public abstract void onError();
}
