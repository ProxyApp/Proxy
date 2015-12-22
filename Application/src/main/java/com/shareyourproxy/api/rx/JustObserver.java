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
        Timber.v("%1$s completed", this.toString());
        complete();
    }

    @Override
    public void onError(Throwable e) {
        Timber.e(Log.getStackTraceString(e));
        error(e);
    }

    @Override
    public void onNext(T t) {
        Timber.v("%1$s onNext obj: %2$s", this.toString(), t.toString());
        next(t);
    }

    public abstract void next(T t);

    public void error(Throwable e) {
    }

    public void complete() {
    }
}
