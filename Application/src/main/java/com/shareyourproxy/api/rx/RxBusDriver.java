package com.shareyourproxy.api.rx;

import com.shareyourproxy.ProxyApplication;

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
    private final Subject<Object, Object> _rxBus =
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
        return _rxBus.compose(RxHelper.applySchedulers());
    }

    /**
     * Post an event on {@link PublishSubject}.
     *
     * @param event event object.
     */
    public void post(Object event) {
        Timber.v("Event Posted: " + event.toString());
        _rxBus.onNext(event);
    }

}
