package com.shareyourproxy.api.rx

import android.content.Context
import com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser
import com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUsers
import com.shareyourproxy.api.domain.model.User
import io.realm.Realm
import rx.Observable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.functions.Func1
import rx.schedulers.Schedulers

/**
 * RxHelper for common rx.Observable method calls.
 */
object RxHelper {
    fun <T> singleObserveMain(): Single.Transformer<T, T> {
        return Single.Transformer<T, T> { single -> single.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
    }

    fun <T> observeMain(): Observable.Transformer<T, T> {
        return Observable.Transformer<T, T> { observable -> observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()) }
    }

    fun <T> observeIO(): Observable.Transformer<T, T> {
        return Observable.Transformer<T, T> { observable -> observable.subscribeOn(Schedulers.io()).observeOn(Schedulers.io()) }
    }

    fun <T> filterNullObject(): Func1<T, Boolean> {
        return Func1 { obj -> obj != null }
    }

    fun updateRealmUser(context: Context, user: User) {
        val realm = Realm.getInstance(context)
        realm.refresh()
        val realmUser = createRealmUser(user)
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(realmUser)
        realm.commitTransaction()
        realm.close()
    }

    fun updateRealmUser(context: Context, users: Map<String, User>) {
        val realm = Realm.getInstance(context)
        realm.refresh()
        val realmUsers = createRealmUsers(users)
        realm.beginTransaction()
        realm.copyToRealmOrUpdate(realmUsers)
        realm.commitTransaction()
        realm.close()
    }
}
