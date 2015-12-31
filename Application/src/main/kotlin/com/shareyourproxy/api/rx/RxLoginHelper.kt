package com.shareyourproxy.api.rx

import android.util.Log
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil.getToken
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.plus.Plus
import com.shareyourproxy.Constants.KEY_LOGGED_IN_USER
import com.shareyourproxy.Constants.KEY_PLAY_INTRODUCTION
import com.shareyourproxy.IntentLauncher.launchLoginActivity
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxBusRelay.post
import com.shareyourproxy.api.rx.RxHelper.singleObserveMain
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.app.BaseActivity
import com.shareyourproxy.app.GoogleApiActivity
import rx.Single
import rx.SingleSubscriber
import rx.functions.Func1
import timber.log.Timber

/**
 * Handle login functions.
 */
object RxLoginHelper {

    fun loginObservable(activity: BaseActivity): Single<User> {
        return Single.create(createLoginFlow(activity)).compose(singleObserveMain<User>())
    }

    private fun createLoginFlow(activity: BaseActivity): Single.OnSubscribe<User> {
        return object : Single.OnSubscribe<User> {
            override fun call(subscriber: SingleSubscriber<in User>) {
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
                                Timber.e(Log.getStackTraceString(e))
                            }
                        }
                        //if there is a user saved in shared prefs
                        if (user != null) {
                            //TODO UPDATE USER VERSION
//                            RestClient(activity).herokuUserService
//                                    .updateUserVersion(user.id, BuildConfig.VERSION_CODE)
//                                    .map(finalUser(user))
//                                    .subscribe(updateUserVersionObserver(subscriber))
                        } else {
                            launchLoginActivity(activity)
                            activity.finish()
                        }
                    }
                    subscriber.onSuccess(user)
                } catch (e: Exception) {
                    // remove the cached user if we fail
                    activity.sharedPreferences.edit().remove(KEY_LOGGED_IN_USER)
                    subscriber.onError(e)
                }
            }

            fun updateUserVersionObserver(subscriber: SingleSubscriber<in User>): JustObserver<User> {
                return object : JustObserver<User>() {
                    @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
                    override fun next(user: User) {
                        RxHelper.updateRealmUser(activity, user!!)
                        post(SyncContactsCommand(user))
                    }

                    override fun error(e: Throwable) {
                        super.error(e)
                        subscriber.onError(e)
                    }
                }
            }

            fun finalUser(user: User): Func1<String, User> {
                return Func1 { user }
            }
        }
    }

    fun refreshGooglePlusToken(activity: GoogleApiActivity, client: GoogleApiClient?): Single<String> {
        return Single.create { subscriber ->
            var token: String? = null
            try {
                if (client!!.isConnected) {
                    token = getToken(activity, Plus.AccountApi.getAccountName(client), "oauth2:%s".format(Scopes.PLUS_LOGIN))
                }
            } catch (e: Exception) {
                subscriber.onError(e)
            } catch(e: UserRecoverableAuthException) {
                activity.startActivityForResult(e.intent, GoogleApiActivity.RC_SIGN_IN);
            } catch (e: GoogleAuthException) {
                subscriber.onError(e)
            }

            if (token != null) {
                subscriber.onSuccess(token)
            } else {
                subscriber.onError(NullPointerException("Null Google Plus Token"))
            }
        }
    }
}
