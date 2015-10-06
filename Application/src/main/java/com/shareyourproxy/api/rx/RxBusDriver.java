package com.shareyourproxy.api.rx;

import android.os.Parcel;
import android.os.Parcelable;

import com.shareyourproxy.ProxyApplication;

import rx.Observable;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;
import timber.log.Timber;

/**
 * A singleton pattern intended to store an instance in the {@link ProxyApplication} that allows one
 * to easily send messages over this {@link PublishSubject} Bus.
 */
public class RxBusDriver implements Parcelable {
    private static final RxBusDriver DEFAULT_INSTANCE = new RxBusDriver();
    public static final Creator<RxBusDriver> CREATOR = new Creator<RxBusDriver>() {
        @Override
        public RxBusDriver createFromParcel(Parcel in) {
            return DEFAULT_INSTANCE;
        }

        @Override
        public RxBusDriver[] newArray(int size) {
            return new RxBusDriver[size];
        }
    };
    private static Subject<Object, Object> _rxBus =
        new SerializedSubject<>(PublishSubject.create());

    /**
     * Private constructor.
     */
    private RxBusDriver() {
    }

    public static RxBusDriver getInstance() {
        return DEFAULT_INSTANCE;
    }

    public Observable<Object> toObservable() {
        return _rxBus.onBackpressureLatest().compose(RxHelper.applySchedulers());
    }

    /**
     * Post an event on {@link PublishSubject}.
     *
     * @param event event object.
     */
    public void post(Object event) {
        Timber.i("Event Posted: " + event.toString());
        _rxBus.onNext(event);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
