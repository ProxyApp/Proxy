package com.shareyourproxy.api.rx;

import android.content.Context;
import android.support.v7.util.SortedList;

import com.shareyourproxy.api.domain.factory.GroupFactory;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.GroupEditChannel;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.callback.CommandEvent;
import com.shareyourproxy.api.rx.command.callback.GroupChannelsUpdatedEvent;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;

import static com.shareyourproxy.api.RestClient.getUserGroupService;
import static com.shareyourproxy.api.domain.factory.RealmUserFactory.createRealmUser;

/**
 * Created by Evan on 6/11/15.
 */
public class RxGroupChannelSync {
    private RxGroupChannelSync() {
    }

    public static List<CommandEvent> updateGroupChannels(
        Context context, User user, String newTitle, Group oldGroup, ArrayList<Channel> channels) {
        return Observable.zip(
            saveRealmGroupChannels(context, user, newTitle, oldGroup, channels),
            saveFirebaseGroupChannels(context, user.id().value(), newTitle, oldGroup, channels),
            zipAddGroupChannels(newTitle, channels))
            .toList().toBlocking().single();
    }

    public static ArrayList<Channel> getSelectedChannels(SortedList<GroupEditChannel> channels) {
        return Observable.just(channels).map(getSelectedChannels()).toBlocking().single();
    }

    private static Func1<SortedList<GroupEditChannel>, ArrayList<Channel>> getSelectedChannels() {
        return new Func1<SortedList<GroupEditChannel>, ArrayList<Channel>>() {
            @Override
            public ArrayList<Channel> call(SortedList<GroupEditChannel> groupEditChannels) {
                ArrayList<Channel> selectedChannels = new ArrayList<>();
                for (int i =0; i < groupEditChannels.size(); i++) {
                    GroupEditChannel editChannel = groupEditChannels.get(i);
                    if (editChannel.inGroup()) {
                        selectedChannels.add(editChannel.getChannel());
                    }
                }
                return selectedChannels;
            }
        };
    }

    private static rx.Observable<Group> saveRealmGroupChannels(
        Context context, User user, String newTitle, Group oldGroup, ArrayList<Channel> channels) {
        return Observable.just(channels)
            .map(addRealmGroupChannels(context, user, newTitle, oldGroup));
    }

    private static Func1<ArrayList<Channel>, Group> addRealmGroupChannels(
        final Context context, final User user, final String newTitle, final Group oldGroup) {
        return new Func1<ArrayList<Channel>, Group>() {
            @Override
            public Group call(ArrayList<Channel> channels) {
                Group newGroup = GroupFactory.addGroupChannels(newTitle, oldGroup, channels);
                User newUser = UserFactory.addUserGroup(user, newGroup);
                Realm realm = Realm.getInstance(context);
                realm.beginTransaction();
                realm.copyToRealmOrUpdate(createRealmUser(newUser));
                realm.commitTransaction();
                realm.close();
                return newGroup;
            }
        };
    }

    private static rx.Observable<Group> saveFirebaseGroupChannels(
        Context context, String userId, String newTitle, Group group, ArrayList<Channel> channels) {
        String groupId = group.id().value();
        return getUserGroupService(context)
            .addUserGroup(userId,groupId, Group.copy(group, newTitle, channels));
    }

    private static Func2<Group, Group, CommandEvent> zipAddGroupChannels(
        final String newTitle, final ArrayList<Channel> channels) {
        return new Func2<Group, Group, CommandEvent>() {
            @Override
            public GroupChannelsUpdatedEvent call(Group group, Group group2) {
                return new GroupChannelsUpdatedEvent(group, channels);
            }
        };
    }
}
