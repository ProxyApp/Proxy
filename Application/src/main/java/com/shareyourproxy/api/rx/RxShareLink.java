package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.SharedLink;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.event.ShareLinkEvent;

import java.util.ArrayList;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by Evan on 7/26/15.
 */
public class RxShareLink {

    public static EventCallback getShareLinkMessageObservable(
        final Context context, final User user, final ArrayList<GroupToggle> groups) {
        return Observable.create(new Observable.OnSubscribe<EventCallback>() {
            @Override
            public void call(final Subscriber<? super EventCallback> subscriber) {
                try {
                    ArrayList<String> groupIds = Observable.just(groups)
                        .map(getCheckedGroups(context)).toBlocking().single();

                    Observable.from(groupIds)
                        .map(queryLinkIds(context, user.id()))
                        .map(generateMessage(context))
                        .subscribe(handleMessage(subscriber));

                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).toBlocking().single();
    }

    public static Func1<String, SharedLink> queryLinkIds(final Context context, final String
        userId) {
        return new Func1<String, SharedLink>() {
            @Override
            public SharedLink call(String groupId) {
                return RestClient.getHerokuUserervice(context).
                    getSharedLink(groupId, userId).toBlocking().single();
            }
        };
    }

    public static Subscriber<String> handleMessage(
        final Subscriber<? super EventCallback> subscriber) {
        return new Subscriber<String>() {
            @Override
            public void onCompleted() {
                subscriber.onCompleted();
            }

            @Override
            public void onError(Throwable e) {
                subscriber.onError(e);
            }

            @Override
            public void onNext(String message) {
                subscriber.onNext(new ShareLinkEvent(message));
            }
        };
    }

    public static Func1<SharedLink, String> generateMessage(final Context context) {
        return new Func1<SharedLink, String>() {
            @Override
            public String call(SharedLink link) {
                StringBuilder sb = new StringBuilder();
                sb.append(context.getString(R.string.sharelink_message_link, link.id()));
                sb.append(System.getProperty("line.separator"));
                sb.append(System.getProperty("line.separator"));
                return sb.toString();
            }
        };
    }

    private static Func1<ArrayList<GroupToggle>, ArrayList<String>> getCheckedGroups(
        final Context context) {
        return new Func1<ArrayList<GroupToggle>, ArrayList<String>>() {
            @Override
            public ArrayList<String> call(ArrayList<GroupToggle> groupToggles) {
                RxGoogleAnalytics analytics = RxGoogleAnalytics.getInstance(context);
                ArrayList<String> checkedGroups = new ArrayList<>(groupToggles.size());
                for (GroupToggle groupEntry : groupToggles) {
                    if (groupEntry.isChecked()) {
                        Group group = groupEntry.getGroup();
                        analytics.shareLinkGenerated(group);
                        checkedGroups.add(group.id());
                    }
                }
                return checkedGroups;
            }
        };
    }
}