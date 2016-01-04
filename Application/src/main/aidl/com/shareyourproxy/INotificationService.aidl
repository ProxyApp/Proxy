package com.shareyourproxy;

import android.app.Notification;

interface INotificationService {
    List<Notification> getNotifications(in String userId);
}
