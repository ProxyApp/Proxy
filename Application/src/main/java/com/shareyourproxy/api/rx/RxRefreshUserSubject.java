package com.shareyourproxy.api.rx;

import android.view.View;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;
import rx.subjects.SerializedSubject;
import rx.subjects.Subject;

/**
 * Created by Evan on 10/4/15.
 */
public class RxRefreshUserSubject {
        private static final RxRefreshUserSubject DEFAULT_INSTANCE = new RxRefreshUserSubject();
        private static final Subject<View, View> _rxBus =
            new SerializedSubject<>(PublishSubject.<View>create());

        /**
         * Private constructor.
         */
        private RxRefreshUserSubject() {
        }

        public static RxRefreshUserSubject getInstance() {
            return DEFAULT_INSTANCE;
        }

        public static Observable<View> toObserverable() {
            return _rxBus.throttleFirst(3, TimeUnit.SECONDS, Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
        }

        /**
         * Post an event on {@link PublishSubject}.
         *
         * @param view clicked.
         */
        public static void click(View view) {
            _rxBus.onNext(view);
        }
}
