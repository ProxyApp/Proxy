package com.shareyourproxy.api.rx

import com.crashlytics.android.answers.Answers
import com.crashlytics.android.answers.CustomEvent
import com.shareyourproxy.api.domain.model.User
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback
import com.shareyourproxy.api.rx.command.eventcallback.ShareLinkEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback
import rx.Observable
import rx.functions.Action1

/**
 * Dump all the analytics for fabric's Answers platform here.
 */
internal object RxFabricAnalytics {
    fun logFabricAnalytics(answers: Answers, realmUser: User, event: EventCallback) {
        Observable.just(event)
                .doOnNext(LogEvent(answers, realmUser))
                .compose(RxHelper.observeMain())
                .subscribe()
    }

    fun LogEvent(answers: Answers, realmUser: User): Action1<EventCallback> {
        return Action1 { event ->
            if (event is UserChannelAddedEventCallback) {
                logChannelAddedEvent(answers, event)
            } else if (event is UserContactAddedEventCallback) {
                logContactAddedEvent(answers, realmUser)
            } else if (event is ShareLinkEventCallback) {
                answers.logCustom(CustomEvent("Share Public Link"))
            }
        }
    }

    private fun logContactAddedEvent(answers: Answers, realmUser: User) {
        val contactCount = realmUser.contacts.size.toString()
        answers.logCustom(CustomEvent("Contact Added").putCustomAttribute("Contact Count", contactCount))
    }

    private fun logChannelAddedEvent(answers: Answers, event: UserChannelAddedEventCallback) {
        val channelType = event.newChannel.channelType.label

        if (event.oldChannel == null) {
            answers.logCustom(CustomEvent("Channel Added").putCustomAttribute("Channel Type", channelType))
        } else {
            answers.logCustom(CustomEvent("Channel Edited").putCustomAttribute("Channel Type", channelType))
        }
    }
}
