package com.shareyourproxy.api.rx;

import android.content.Context;

import com.shareyourproxy.R;
import com.shareyourproxy.api.RestClient;
import com.shareyourproxy.api.domain.model.GroupToggle;
import com.shareyourproxy.api.domain.model.SharedLink;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.event.ShareLinkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

/**
 * Created by Evan on 7/26/15.
 */
public class RxShareLink {

    public static List<EventCallback> getShareLinkMessageObservable(
        final Context context, final RxBusDriver rxBus, final ArrayList<GroupToggle> groups) {
        return Observable.create(new Observable.OnSubscribe<EventCallback>() {
            @Override
            public void call(final Subscriber<? super EventCallback> subscriber) {
                try {
                    ArrayList<String> groupIds = Observable.just(groups)
                        .map(getCheckedGroups()).toBlocking().single();
                    final HashMap<String, SharedLink> links =
                        RestClient.getSharedLinkService(context, rxBus)
                            .getSharedLinks().toBlocking().single();

                    String message = Observable.from(groupIds)
                        .map(sortSharedLinks(links))
                        .filter(RxHelper.filterNullObject())
                        .buffer(groupIds.size())
                        .map(buildMessage(context))
                        .toBlocking().single();

                    subscriber.onNext(new ShareLinkEvent(message));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).toList().toBlocking().single();
    }

    private static Func1<List<SharedLink>, String> buildMessage(final Context context) {
        return new Func1<List<SharedLink>, String>() {
            @Override
            public String call(List<SharedLink> sharedLinks) {
                StringBuilder sb = new StringBuilder();
                for (SharedLink link : sharedLinks) {
                    sb.append(context.getString(
                        R.string.sharelink_message_link, link.id()));
                    sb.append(System.getProperty("line.separator"));
                    sb.append(System.getProperty("line.separator"));
                }
                return sb.toString();
            }
        };
    }

    private static Func1<String, SharedLink> sortSharedLinks(
        final HashMap<String, SharedLink> links) {
        return new Func1<String, SharedLink>() {
            @Override
            public SharedLink call(String groupId) {
                for (Map.Entry<String, SharedLink> linkEntry : links.entrySet()) {
                    SharedLink link = linkEntry.getValue();
                    if (link.groupId().equals(groupId)) {
                        return link;
                    }
                }
                return null;
            }
        };
    }

    private static Func1<ArrayList<GroupToggle>, ArrayList<String>> getCheckedGroups() {
        return new Func1<ArrayList<GroupToggle>, ArrayList<String>>() {
            @Override
            public ArrayList<String> call(
                ArrayList<GroupToggle>
                    groupToggles) {
                ArrayList<String> checkedGroups = new ArrayList<>(groupToggles
                    .size());
                for (GroupToggle groupEntry : groupToggles) {
                    if (groupEntry.isChecked()) {
                        checkedGroups.add(groupEntry.getGroup().id());
                    }
                }
                return checkedGroups;
            }
        };
    }
}