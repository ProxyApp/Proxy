package com.shareyourproxy.api.rx;

import android.util.Log;

import rx.Observer;
import timber.log.Timber;

/**
 * This abstraction simply logs all errors to the command prompt.
 */
public abstract class JustObserver<T> implements Observer<T> {

    @Override
    public void onCompleted() {
        Timber.v(this.toString(), "onComplete");
    }

    @Override
    public void onError(Throwable e) {
        Timber.e(Log.getStackTraceString(e));
        this.error(e);
    }

    @Override
    public void onNext(T t) {
        this.next(t);
    }

    public abstract void next(T t);
    public abstract void error(Throwable e);
}
