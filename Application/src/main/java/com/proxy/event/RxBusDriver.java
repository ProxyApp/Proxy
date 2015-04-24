package com.proxy.event;

import com.proxy.ProxyApplication;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 * A singleton pattern intended to store an Instance in the {@link ProxyApplication} that allows one
 * to easily send messages over this {@link PublishSubject} Bus.
 */
@SuppressWarnings("unused")
public class RxBusDriver {
    private static final RxBusDriver DEFAULT_INSTANCE = new RxBusDriver();
    private final Subject<Object, Object> rxBus =
        new SerializedSubject<>(PublishSubject.create());

    /**
     * Private constructor.
     */
    private RxBusDriver() {
    }

    public static RxBusDriver getInstance() {
        return DEFAULT_INSTANCE;
    }

    public Observable<Object> toObserverable() {
        return rxBus;
    }

    /**
     * Post an event on {@link PublishSubject}.
     *
     * @param event event object.
     */
    @DebugLog
    public void post(Object event) {
        Timber.v("Event Posted: " + event.toString());
        rxBus.onNext(event);
    }

}
