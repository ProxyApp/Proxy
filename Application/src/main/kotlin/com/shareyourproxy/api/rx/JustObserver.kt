package com.shareyourproxy.api.rx

import android.util.Log
import rx.Observer
import timber.log.Timber

/**
 * This abstraction simply logs all errors to the command prompt.
 */
internal abstract class JustObserver<T>(private val clazz: Class<*>) : Observer<T> {

    override fun onCompleted() {
        Timber.v("${clazz.simpleName} completed")
        complete()
    }

    override fun onError(e: Throwable) {
        Timber.e("${clazz.simpleName}: ${Log.getStackTraceString(e)}")
        error(e)
    }

    override fun onNext(t: T) {
        Timber.v("${clazz.simpleName} onNext obj: ${t.toString()}")
        next(t)
    }

    abstract fun next(t: T)

    open fun error(e: Throwable) {
    }

    open fun complete() {
    }
}
