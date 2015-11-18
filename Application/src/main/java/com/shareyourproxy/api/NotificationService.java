package com.shareyourproxy.api;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import com.shareyourproxy.INotificationService;
import com.shareyourproxy.api.rx.RxBusDriver;
import com.shareyourproxy.api.rx.command.GetUserMessagesCommand;
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;
import com.shareyourproxy.api.rx.command.eventcallback.UserMessagesDownloadedEventCallback;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Evan on 6/18/15.
 */
public class NotificationService extends Service {
    private final INotificationService.Stub _binder = new INotificationService.Stub() {
        @Override
        public ArrayList<Notification> getNotifications(String userId)
            throws RemoteException {
            List<EventCallback> event = new GetUserMessagesCommand(userId)
                .execute(NotificationService.this);
            EventCallback eventData = event.get(0);

            if (eventData != null) {
                ArrayList<Notification> notifications =
                    ((UserMessagesDownloadedEventCallback) eventData).notifications;
                if (notifications != null) {
                    Timber.i("Message Downloaded");
                    return notifications;
                }
            }
            Timber.i("No Messages");
            return new ArrayList<>();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }
}
