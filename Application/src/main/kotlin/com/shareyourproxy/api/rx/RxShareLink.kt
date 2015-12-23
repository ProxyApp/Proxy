package com.shareyourproxy.api.rx

import android.content.Context
import com.shareyourproxy.R
import com.shareyourproxy.api.RestClient
import com.shareyourproxy.api.domain.model.GroupToggle
import com.shareyourproxy.api.domain.model.SharedLink
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.ShareLinkEventCallback
import rx.Observable
import rx.Observable.create
import rx.Subscriber
import rx.functions.Func1
import java.util.*

/**
 * Handle shared link queue.
 */
object RxShareLink {
    fun getShareLinkMessageObservable(context: Context, user: User, groups: ArrayList<GroupToggle>): EventCallback {
        return create(getSharedLinks(context, groups, user)).toBlocking().single()
    }

    private fun getSharedLinks(context: Context, groups: ArrayList<GroupToggle>, user: User): Observable.OnSubscribe<EventCallback> {
        return Observable.OnSubscribe<EventCallback> { subscriber ->
            try {
                val groupIds = Observable.just(groups).map(getCheckedGroups(context)).toBlocking().single()
                Observable.from(groupIds).map(queryLinkIds(user.id)).map(generateMessage(context)).subscribe(handleMessage(subscriber))
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    fun queryLinkIds(userId: String): Func1<String, SharedLink> {
        return Func1 { groupId -> RestClient.herokuUserService.getSharedLink(groupId, userId).toBlocking().single() }
    }

    fun handleMessage(subscriber: Subscriber<in EventCallback>): Subscriber<String> {
        return object : Subscriber<String>() {
            override fun onCompleted() {
                subscriber.onCompleted()
            }

            override fun onError(e: Throwable) {
                subscriber.onError(e)
            }

            override fun onNext(message: String) {
                subscriber.onNext(ShareLinkEventCallback(message))
            }
        }
    }

    fun generateMessage(context: Context): Func1<SharedLink, String> {
        return Func1 { link ->
            val sb = StringBuilder()
            sb.append(context.getString(R.string.sharelink_message_link, link.id))
            sb.append(System.getProperty("line.separator"))
            sb.append(System.getProperty("line.separator"))
            sb.toString()
        }
    }

    private fun getCheckedGroups(context: Context): Func1<ArrayList<GroupToggle>, ArrayList<String>> {
        return Func1 { groupToggles ->
            val analytics = RxGoogleAnalytics(context)
            val checkedGroups = ArrayList<String>(groupToggles.size)
            for (groupEntry in groupToggles) {
                if (groupEntry.isChecked) {
                    val group = groupEntry.group
                    analytics.shareLinkGenerated(group)
                    checkedGroups.add(group.id)
                }
            }
            checkedGroups
        }
    }
}

