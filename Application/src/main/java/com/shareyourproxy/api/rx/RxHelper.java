package com.shareyourproxy.api.rx;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Evan on 5/21/15.
 */
public class RxHelper {
    @SuppressWarnings("unchecked")
    public static Observable.Transformer<T, T> _schedulersTransformer =
        new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) _schedulersTransformer;
    }

    interface T {
    }
}
