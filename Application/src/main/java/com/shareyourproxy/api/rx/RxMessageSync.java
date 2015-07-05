package com.shareyourproxy.api.rx;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.shareyourproxy.R;
import com.shareyourproxy.api.domain.factory.UserFactory;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Message;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserMessageAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback;
import com.shareyourproxy.app.UserProfileActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;
import rx.observables.BlockingObservable;

import static com.shareyourproxy.Intents.getUserProfileIntent;
import static com.shareyourproxy.api.RestClient.getMessageService;
import static com.shareyourproxy.util.ObjectUtils.joinWithSpace;

/**
 * Cold Rx.Observable calls to handle syncing messages for Users.
 */
public class RxMessageSync {

    /**
     * Private Constructor.
     */
    private RxMessageSync() {
    }

    public static List<EventCallback> getFirebaseMessages(
        final Context context, @NonNull User user) {
        final String userId = user.id().value();
        return getMessageService()
            .getUserMessages(userId).map(new Func1<Map<String, Contact>, EventCallback>() {
                @Override
                public EventCallback call(Map<String, Contact> contacts) {
                    ArrayList<Notification> notifications = new ArrayList<>();
                    if (contacts != null && contacts.size() > 0) {
                        for (Map.Entry<String, Contact> message : contacts.entrySet()) {
                            String firstName = message.getValue().first();
                            String lastName = message.getValue().last();

                            NotificationCompat.Builder _builder =
                                new NotificationCompat.Builder(context)
                                    .setLargeIcon(getProxyIcon(context))
                                    .setSmallIcon(R.mipmap.ic_proxy)
                                    .setAutoCancel(true)
                                    .setVibrate(new long[]{ 1000, 1000 })
                                    .setLights(Color.MAGENTA, 1000, 1000)
                                    .setContentTitle(context.getString(R.string.app_name))
                                    .setContentText(context.getString(R.string.added_to_contacts,
                                        joinWithSpace(new String[]{ firstName, lastName })))
                                    .setContentIntent(
                                        getPendingUserProfileIntent(
                                            context, userId, message.getValue()));

                            notifications.add(_builder.build());
                        }
                        return new UserMessagesDownloadedEventCallback(notifications);
                    }
                    return null;
                }
            })
            .toList()
            .toBlocking()
            .single();
    }

    public static Bitmap getProxyIcon(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_proxy);
    }

    public static PendingIntent getPendingUserProfileIntent(
        Context context, String loggedInUserId, Contact contact) {
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent =
            getUserProfileIntent(UserFactory.createModelUser(contact), loggedInUserId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(UserProfileActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static List<EventCallback> saveFirebaseMessage(String userId, Message message) {
        String contactId = message.contact().id().value();
        Contact contact = message.contact();
        HashMap<String, Contact> contactEntry = new HashMap<>(1);
        contactEntry.put(contactId, contact);
        return getMessageService()
            .addUserMessage(userId, contactEntry)
            .map(getUserMessageCallback())
            .toList()
            .compose(RxHelper.<List<EventCallback>>applySchedulers())
            .toBlocking().single();
    }

    private static Func1<Message, EventCallback> getUserMessageCallback() {
        return new Func1<Message, EventCallback>() {
            @Override
            public EventCallback call(Message message) {
                return new UserMessageAddedEventCallback(message);
            }
        };
    }

    public static BlockingObservable<Message> deleteFirebaseMessage(Message message) {
        String contactId = message.contact().id().value();
        return getMessageService()
            .deleteUserMessage(contactId, message.id().value())
            .compose(RxHelper.<Message>applySchedulers())
            .toBlocking();
    }

    public static Observable<Message> deleteAllFirebaseMessages(User user) {
        String contactId = user.id().value();
        return getMessageService()
            .deleteAllUserMessages(contactId)
            .compose(RxHelper.<Message>applySchedulers());
    }


}
