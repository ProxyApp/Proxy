package com.shareyourproxy.api.rx

import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.util.concurrent.TimeUnit

/**
 * Subject to watch EditTextViews.
 */
object RxTextWatcherRelay {
    private val bus: PublishRelay<String> = PublishRelay.create();
    fun textWatcherObserverable(): Observable<String> {
        return Observable.defer { bus.toSerialized().debounce(500, TimeUnit.MILLISECONDS, Schedulers.io())
                .onBackpressureLatest()
                .observeOn(AndroidSchedulers.mainThread())}
    }

    /**
     * Post an event on [PublishSubject].
     * @param searchText String.
     */
    fun post(searchText: String) {
        bus.call(searchText)
    }
}