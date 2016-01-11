package com.shareyourproxy.api.rx

import com.shareyourproxy.BuildConfig.VERSION_CODE
import com.shareyourproxy.Constants.KEY_LOGGED_IN_USER
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchLoginActivity
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxHelper.observeMain
import com.shareyourproxy.api.rx.RxHelper.updateRealmUser
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.app.BaseActivity
import rx.Single
import rx.SingleSubscriber
import rx.Subscription

/**
 * Handle login functions.
 */
internal object RxLoginHelper {

    fun loginSubscription(activity: BaseActivity): Subscription {
        return Single.create(createLoginFlow(activity))
                .compose(RxHelper.singleObserveMain<Int>())
                .subscribe(loginObserver())
    }

    private fun loginObserver(): JustSingle<Int> {
        return object : JustSingle<Int>(RxLoginHelper::class.java) {}
    }

    private fun createLoginFlow(activity: BaseActivity): Single.OnSubscribe<Int> {
        return object : Single.OnSubscribe<Int> {
            override fun call(subscriber: SingleSubscriber<in Int>) {
                var user: User? = null
                try {
                    //even if we have a user saved, if this isn't present, go to login.
                    if (activity.sharedPreferences.getBoolean(KEY_PLAY_INTRODUCTION, false)) {
                        launchLoginActivity(activity)
                        activity.finish()
                    } else {
                        //get the shared preferences user
                        val jsonUser = activity.sharedPreferences.getString(KEY_LOGGED_IN_USER, null)
                        if (jsonUser != null) {
                            try {
                                user = activity.sharedPrefJsonUser
                            } catch (e: Exception) {
                                subscriber.onError(e)
                            }
                        }
                        //if there is a user saved in shared prefs
                        if (user != null) {
                            RestClient(activity).herokuUserService
                                    .updateUserVersion(user.id, VERSION_CODE)
                                    .compose(observeMain<Int>())
                                    .subscribe(updateUserVersionObserver(user, subscriber))
                        } else {
                            launchLoginActivity(activity)
                            activity.finish()
                        }
                    }
                } catch (e: Exception) {
                    // remove the cached user if we fail
                    activity.sharedPreferences.edit().remove(KEY_LOGGED_IN_USER).commit()
                    subscriber.onError(e)
                }
            }

            fun updateUserVersionObserver(user: User, subscriber: SingleSubscriber<in Int>): JustObserver<Int> {
                return object : JustObserver<Int>(RxLoginHelper::class.java) {
                    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                    override fun next(version: Int) {
                        updateRealmUser(activity, user)
                        post(SyncContactsCommand(user))
                        subscriber.onSuccess(version)
                    }

                    override fun error(e: Throwable) {
                        super.error(e)
                        launchLoginActivity(activity)
                        subscriber.onError(e)
                    }
                }
            }
        }
    }
}
