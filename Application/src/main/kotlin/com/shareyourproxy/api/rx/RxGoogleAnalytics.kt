package com.shareyourproxy.api.rx

import android.content.Context
import com.google.android.gms.analytics.GoogleAnalytics
import com.google.android.gms.analytics.HitBuilders
import com.google.android.gms.analytics.Tracker
import com.shareyourproxy.R
import com.shareyourproxy.api.domain.model.ChannelType
import com.shareyourproxy.api.domain.model.Group
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.RxHelper.singleObserveMain
import rx.Single
import rx.Subscription

/**
 * Log Google Analytics instances.
 */
internal final class RxGoogleAnalytics(context: Context) {
    val analytics: GoogleAnalytics = GoogleAnalytics.getInstance(context)
    val tracker: Tracker = analytics.newTracker(R.xml.global_tracker)

    fun userAdded(newUser: User): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("Add User").setAction("Google Plus").setLabel(newUser.fullName).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun channelAdded(channelType: ChannelType): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("Channel Event").setAction("Add Channel").setLabel(channelType.label).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun channelEdited(oldChannelType: ChannelType): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("Channel Event").setAction("Edit Channel").setLabel(oldChannelType.label).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun userProfileViewed(user: User): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("User Event").setAction("LoggedInUser Profile View").setLabel(user.fullName).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun contactProfileViewed(user: User): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("User Event").setAction("Contact Profile View").setLabel(user.fullName).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun userContactAdded(user: User): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("User Event").setAction("User Contact Added").setLabel(user.fullName).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun userContactRemoved(user: User): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("User Event").setAction("User Contact Removed").setLabel(user.fullName).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun shareLinkGenerated(group: Group): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("Share Link").setAction("Link Generated").setLabel(group.label).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }

    fun contactGroupButtonHit(): Subscription {
        return Single.create(Single.OnSubscribe<kotlin.Boolean> { singleSubscriber ->
            try {
                tracker.send(HitBuilders.EventBuilder().setCategory("User Event").setAction("Group Contact Button Hit").setValue(1).build())
                singleSubscriber.onSuccess(true)
            } catch (e: Exception) {
                singleSubscriber.onError(e)
            }
        }).compose(singleObserveMain<Boolean>()).subscribe()
    }
}
