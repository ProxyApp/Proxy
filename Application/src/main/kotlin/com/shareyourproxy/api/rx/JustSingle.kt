package com.shareyourproxy.api.rx

import android.util.Log

import rx.SingleSubscriber
import timber.log.Timber

/**
 * Single subscriber that logs.
 */
internal abstract class JustSingle<T>(private val clazz:Class<*>) : SingleSubscriber<T>() {
    override fun onSuccess(value: T) {
        Timber.v("${clazz.simpleName} Success obj: ${value.toString()}")
    }

    override fun onError(e: Throwable) {
        Timber.e("${clazz.simpleName}: ${Log.getStackTraceString(e)}")
    }
}
