package com.shareyourproxy.api.rx;

import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserChannelAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserContactAddedEventCallback;
import com.shareyourproxy.api.rx.event.ShareLinkEvent;

import rx.Observable;
import rx.functions.Action1;

/**
 * Dump all the analytics for fabric's Answers platform here.
 */
public class RxFabricAnalytics {

    /**
     * Private Constructor.
     */
    private RxFabricAnalytics() {
    }

    public static void logAnalytics(
        final Answers answers, final User realmUser, EventCallback event) {
        Observable.just(event).doOnNext(LogEvent(answers, realmUser)).compose(RxHelper
            .subThreadObserveMain()).subscribe();
    }

    public static Action1<EventCallback> LogEvent(final Answers answers, final User realmUser) {
        return new Action1<EventCallback>() {
            @Override
            public void call(EventCallback event) {
                if (event instanceof UserChannelAddedEventCallback) {
                    logChannelAddedEvent(answers, (UserChannelAddedEventCallback) event);
                } else if (event instanceof UserContactAddedEventCallback) {
                    logContactAddedEvent(answers, realmUser);
                } else if (event instanceof ShareLinkEvent) {
                    answers.logCustom(new CustomEvent("Share Public Link"));
                }
            }
        };
    }

    private static void logContactAddedEvent(Answers answers, User realmUser) {
        String contactCount = String.valueOf(realmUser.contacts().size());
        answers.logCustom(new CustomEvent("Contact Added")
            .putCustomAttribute("Contact Count", contactCount));
    }

    private static void logChannelAddedEvent(Answers answers, UserChannelAddedEventCallback event) {
        String channelType = event
            .newChannel.channelType().getLabel();

        if (event.oldChannel == null) {
            answers.logCustom(new CustomEvent("Channel Added")
                .putCustomAttribute("Channel Type", channelType));
        } else {
            answers.logCustom(new CustomEvent("Channel Edited")
                .putCustomAttribute("Channel Type", channelType));
        }
    }
}
