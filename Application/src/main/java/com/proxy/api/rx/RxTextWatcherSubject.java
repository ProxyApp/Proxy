package com.proxy.api.rx;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by Evan on 5/21/15.
 */
public class RxTextWatcherSubject {
    private static final RxTextWatcherSubject DEFAULT_INSTANCE = new RxTextWatcherSubject();
    private final Subject<String, String> _rxBus =
        new SerializedSubject<>(PublishSubject.<String>create());

    /**
     * Private constructor.
     */
    private RxTextWatcherSubject() {
    }

    public static RxTextWatcherSubject getInstance() {
        return DEFAULT_INSTANCE;
    }

    public Observable<String> toObserverable() {
        return _rxBus.debounce(300, TimeUnit.MILLISECONDS, Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Post an event on {@link PublishSubject}.
     *
     * @param searchText String.
     */
    public void post(String searchText) {
        _rxBus.onNext(searchText);
    }
}
