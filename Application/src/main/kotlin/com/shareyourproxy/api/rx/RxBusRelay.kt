package com.shareyourproxy.api.rx

import android.os.Parcel
import android.os.Parcelable
import com.jakewharton.rxrelay.PublishRelay
import com.shareyourproxy.util.BaseParcelable
import rx.Observable
import timber.log.Timber

/**
 * A singleton pattern intended to store an instance in the [ProxyApplication] that allows one
 * to easily send messages over this [PublishSubject] Bus.
 */
internal object RxBusRelay : BaseParcelable {
    @JvmField val CREATOR: Parcelable.Creator<RxBusRelay> = object : Parcelable.Creator<RxBusRelay> {
        override fun createFromParcel(parcel: Parcel): RxBusRelay {
            return this@RxBusRelay
        }

        override fun newArray(size: Int): Array<RxBusRelay> {
            return Array(size, { i -> this@RxBusRelay })
        }
    }

    private val bus: PublishRelay<Any> = PublishRelay.create();

    fun rxBusObservable(): Observable<Any> {
        return Observable.defer { bus.toSerialized().onBackpressureLatest().compose<Any>(RxHelper.observeMain()) }
    }

    fun ioThreadObservable(): Observable<Any> {
        return Observable.defer { bus.toSerialized().onBackpressureLatest().compose<Any>(RxHelper.observeIO()) }
    }

    /**
     * Post an event on [PublishSubject].
     * @param event event object.
     */
    fun post(event: Any?) {
        if (event != null) {
            Timber.i("Event Posted: ${event.toString()}")
            bus.call(event)
        }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
    }
}

