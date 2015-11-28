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
import com.shareyourproxy.api.domain.model.Message;
import com.shareyourproxy.api.domain.model.User;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserMessageAddedEventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback;
import com.shareyourproxy.app.UserContactActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.functions.Func1;

import static com.shareyourproxy.Intents.getUserProfileIntent;
import static com.shareyourproxy.api.RestClient.getMessageService;
import static com.shareyourproxy.api.rx.RxQuery.getRealmUser;

/**
 * Cold Rx.Observable calls to handle syncing messages for Users.
 */
public class RxMessageSync {

    /**
     * Private Constructor.
     */
    private RxMessageSync() {
    }

    public static EventCallback getFirebaseMessages(
        final Context context, @NonNull final String userId) {
        return getMessageService(context)
            .getUserMessages(userId).map(new Func1<Map<String, Message>, EventCallback>() {
                @Override
                public EventCallback call(Map<String, Message> messages) {
                    ArrayList<Notification> notifications = new ArrayList<>();
                    if (messages == null) {
                        return new UserMessagesDownloadedEventCallback(notifications);
                    }else {
                        for (Map.Entry<String, Message> message : messages.entrySet()) {
                            String fullName = message.getValue().fullName();
                            PendingIntent intent = getPendingUserProfileIntent(
                                context, userId, message.getValue());

                            NotificationCompat.Builder _builder =
                                new NotificationCompat.Builder(context)
                                    .setLargeIcon(getProxyIcon(context))
                                    .setSmallIcon(R.mipmap.ic_proxy_notification)
                                    .setAutoCancel(true)
                                    .setVibrate(new long[]{ 1000, 1000 })
                                    .setLights(Color.MAGENTA, 1000, 1000)
                                    .setContentTitle(context.getString(R.string.app_name))
                                    .setContentText(context.getString(
                                        R.string.added_you, fullName))
                                    .setContentIntent(intent);

                            notifications.add(_builder.build());
                        }
                        return new UserMessagesDownloadedEventCallback(notifications);
                    }
                }
            })
            .compose(RxHelper.<EventCallback>subThreadObserveMain())
            .toBlocking()
            .single();
    }

    public static EventCallback saveFirebaseMessage(
        Context context, String userId, Message message) {
        HashMap<String, Message> messages = new HashMap<>();
        messages.put(message.id(), message);
        return getMessageService(context)
            .addUserMessage(userId, messages)
            .map(getUserMessageCallback())
            .compose(RxHelper.<EventCallback>subThreadObserveMain())
            .toBlocking().single();
    }

    public static Observable<Message> deleteAllFirebaseMessages(
        Context context, User user) {
        String contactId = user.id();
        return getMessageService(context)
            .deleteAllUserMessages(contactId)
            .compose(RxHelper.<Message>subThreadObserveMain());
    }

    private static Bitmap getProxyIcon(Context context) {
        return BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_proxy);
    }

    private static PendingIntent getPendingUserProfileIntent(
        Context context, String loggedInUserId, Message message) {
        // Creates an explicit intent for an Activity in your app
        User contact = getRealmUser(context, message.contactId());
        Intent resultIntent =
            getUserProfileIntent(contact, loggedInUserId);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(UserContactActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Func1<HashMap<String, Message>, EventCallback> getUserMessageCallback() {
        return new Func1<HashMap<String, Message>, EventCallback>() {
            @Override
            public EventCallback call(HashMap<String, Message> message) {
                return new UserMessageAddedEventCallback(message);
            }
        };
    }
}
