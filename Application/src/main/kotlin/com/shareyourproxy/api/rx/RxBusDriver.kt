package com.shareyourproxy.api.rx

import android.os.Parcel
import android.os.Parcelable
import rx.Observable
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import timber.log.Timber

/**
 * A singleton pattern intended to store an instance in the [ProxyApplication] that allows one
 * to easily send messages over this [PublishSubject] Bus.
 */
object RxBusDriver : Parcelable {
    val CREATOR: Parcelable.Creator<RxBusDriver> = object : Parcelable.Creator<RxBusDriver> {
        override fun createFromParcel(parcel: Parcel): RxBusDriver {
            return this@RxBusDriver
        }

        override fun newArray(size: Int): Array<RxBusDriver> {
            return Array(size, { i -> this@RxBusDriver })
        }
    }

    private val _rxBus = SerializedSubject(PublishSubject.create<Any>())

    fun rxBusObservable(): Observable<Any> {
        return _rxBus.onBackpressureLatest().compose<Any>(RxHelper.observeMain())
    }

    fun toIOThreadObservable(): Observable<Any> {
        return _rxBus.onBackpressureLatest().compose<Any>(RxHelper.observeIO())
    }

    /**
     * Post an event on [PublishSubject].

     * @param event event object.
     */
    fun post(event: Any?) {
        if(event!=null) {
            Timber.i("Event Posted: ${event.toString()}")
            _rxBus.onNext(event)
        }
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
    }
}

