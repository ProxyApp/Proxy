package com.shareyourproxy.api.rx;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by Evan on 5/21/15.
 */
public class RxHelper {

    @SuppressWarnings("unchecked")
    public static <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> observable) {
                return observable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
            }
        };
    }

    public static <T> Func1<T, Boolean> filterNullContact() {
        return new Func1<T, Boolean>() {
            @Override
            public Boolean call(T object) {
                return object != null;
            }
        };
    }
}
