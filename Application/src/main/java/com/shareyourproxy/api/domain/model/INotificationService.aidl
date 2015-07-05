package com.shareyourproxy.api.domain.model;

import com.shareyourproxy.api.domain.model.AutoParcel_User;
import android.app.Notification;

interface INotificationService {
    List<Notification> getNotifications(in AutoParcel_User user);
}
