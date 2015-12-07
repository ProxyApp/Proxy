package com.shareyourproxy.api.rx

import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.PublishSubject
import rx.subjects.SerializedSubject
import java.util.concurrent.TimeUnit

/**
 * Created by Evan on 5/21/15.
 */
object RxTextWatcherSubject {
    private val _rxBus = SerializedSubject(PublishSubject.create<String>())
    fun toObserverable(): Observable<String> {
        return _rxBus.debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())
    }

    /**
     * Post an event on [PublishSubject].
     * @param searchText String.
     */
    fun post(searchText: String) {
        _rxBus.onNext(searchText)
    }
}