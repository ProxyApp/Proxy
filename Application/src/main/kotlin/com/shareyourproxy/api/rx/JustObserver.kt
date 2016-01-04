package com.shareyourproxy.api.rx

import android.util.Log

import rx.Observer
import timber.log.Timber

/**
 * This abstraction simply logs all errors to the command prompt.
 */
internal abstract class JustObserver<T> : Observer<T> {

    override fun onCompleted() {
        Timber.v("${this.toString()} completed")
        complete()
    }

    override fun onError(e: Throwable) {
        Timber.e(Log.getStackTraceString(e))
        error(e)
    }

    override fun onNext(t: T) {
        Timber.v("${this.toString()} onNext obj: ${t.toString()}")
        next(t)
    }

    abstract fun next(t: T)

    open fun error(e: Throwable) {
    }

    open fun complete() {
    }
}
