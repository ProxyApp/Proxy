package com.shareyourproxy.api.aidl;

import android.app.Notification;

interface INotificationService {
    List<Notification> getNotifications(in String userId);
}
