package com.shareyourproxy.api.rx

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.firebase.client.AuthData
import com.firebase.client.Firebase
import com.firebase.client.FirebaseError
import com.google.android.gms.auth.GoogleAuthUtil.getToken
import com.google.android.gms.common.Scopes
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.plus.Plus
import com.shareyourproxy.BuildConfig
import com.shareyourproxy.Constants
import com.shareyourproxy.Constants.*
import com.shareyourproxy.IntentLauncher.launchLoginActivity
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.command.SyncContactsCommand
import com.shareyourproxy.app.BaseActivity
import com.shareyourproxy.app.GoogleApiActivity
import rx.Observable
import rx.Single
import rx.SingleSubscriber
import rx.Subscriber
import rx.functions.Action1
import rx.functions.Func1
import timber.log.Timber

/**
 * Handle login functions.
 */
object RxLoginHelper {
    private val _firebaseRef = Firebase(BuildConfig.FIREBASE_ENDPOINT)
    private val _rxHelper = RxHelper

    fun loginObservable(activity: BaseActivity): Single<User> {
        return Single.create(
                object : Single.OnSubscribe<User> {
                    override fun call(subscriber: SingleSubscriber<in User>) {
                        var user: User? = null
                        try {
                            //even if we have a user saved, if this isnt
                            // present, go to login.
                            if (activity.sharedPreferences.getBoolean(KEY_PLAY_INTRODUCTION, false)) {
                                launchLoginActivity(activity)
                                activity.finish()
                            } else {
                                //get the shared preferences user
                                val jsonUser = activity.sharedPreferences.getString(Constants.KEY_LOGGED_IN_USER, null)
                                if (jsonUser != null) {
                                    try {
                                        user = activity.sharedPrefJsonUser
                                    } catch (e: Exception) {
                                        Timber.e(Log.getStackTraceString(e))
                                    }

                                }
                                // if there is a user saved in shared prefs
                                if (user != null) {
                                    RestClient.getUserService(activity).updateUserVersion(user.id(), BuildConfig.VERSION_CODE).map(finalUser(user)).subscribe(updateUserVersionObserver(subscriber))
                                } else {
                                    launchLoginActivity(activity)
                                    activity.finish()
                                }
                            }
                        } catch (e: Exception) {
                            subscriber.onError(e)
                        } finally {
                            subscriber.onSuccess(user)
                        }
                    }

                    fun updateUserVersionObserver(
                            subscriber: SingleSubscriber<in User>): JustObserver<User> {
                        return object : JustObserver<User>() {
                            override fun next(user: User) {
                                activity.loggedInUser = user
                                _rxHelper.updateRealmUser(activity, user)
                                activity.rxBus.post(SyncContactsCommand(user))
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
                }).compose(_rxHelper.singleObserveMain<User>())
    }

    fun refreshFirebaseAuth(
            context: Context, client: GoogleApiClient, sharedPref: SharedPreferences): Observable<String> {
        return Observable.create(Observable.OnSubscribe<kotlin.String> { subscriber ->
            refreshGooglePlusToken(context, client).doOnNext(
                    getFirebaseToken(subscriber, sharedPref)).subscribe()
        }).compose(_rxHelper.observeMain<String>())
    }

    private fun getFirebaseToken(
            subscriber: Subscriber<in String>, sharedPref: SharedPreferences): Action1<String> {
        return Action1 { token -> _firebaseRef.authWithOAuthToken(PROVIDER_GOOGLE, token, getHandler(subscriber, sharedPref)) }
    }

    fun refreshGooglePlusToken(
            context: Context, client: GoogleApiClient): Observable<String> {
        return Observable.create { subscriber ->
            var token: String? = null
            try {
                if (client.isConnected) {
                    token = getToken(context, Plus.AccountApi.getAccountName(client), "oauth2:%s".format(Scopes.PLUS_LOGIN))
                }
            } catch (e: Exception) {
                subscriber.onError(e)
            }

            if (token != null) {
                subscriber.onNext(token)
                subscriber.onCompleted()
            } else {
                subscriber.onError(NullPointerException("Null Google Plus Token"))
            }
        }
    }

    private fun getHandler(
            subscriber: Subscriber<in String>, sharedPref: SharedPreferences): Firebase.AuthResultHandler {
        return object : Firebase.AuthResultHandler {
            @SuppressLint("CommitPrefEdits")
            override fun onAuthenticated(authData: AuthData) {
                val token = authData.token
                sharedPref.edit().putString(KEY_GOOGLE_PLUS_AUTH, token).commit()
                subscriber.onNext(token)
                subscriber.onCompleted()
            }

            override fun onAuthenticationError(firebaseError: FirebaseError) {
                Timber.e(firebaseError.message)
                subscriber.onError(firebaseError.toException())
            }
        }
    }

    /**
     * Utility class for authentication results
     */
    class AuthResultHandler(private val activity: GoogleApiActivity) : Firebase.AuthResultHandler {

        override fun onAuthenticated(authData: AuthData) {
            activity.sharedPreferences.edit().putString(KEY_GOOGLE_PLUS_AUTH, authData.token).commit()
            activity.onAuthenticated(authData)
        }

        override fun onAuthenticationError(firebaseError: FirebaseError) {
            activity.onAuthenticationError(firebaseError.toException())
        }

    }
}
