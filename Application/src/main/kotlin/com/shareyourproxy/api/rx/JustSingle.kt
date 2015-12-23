package com.shareyourproxy.api.rx

import android.util.Log

import rx.SingleSubscriber
import timber.log.Timber

/**
 * Single subscriber that logs.
 */
class JustSingle<T> : SingleSubscriber<T>() {
    override fun onSuccess(value: T) {
        Timber.v("${this.toString()} Success obj: ${value.toString()}")
    }

    override fun onError(e: Throwable) {
        Timber.e(Log.getStackTraceString(e))
    }
}
