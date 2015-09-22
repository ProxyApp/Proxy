package com.shareyourproxy;

import android.app.Notification;
import com.shareyourproxy.api.rx.RxBusDriver;

interface INotificationService {
    List<Notification> getNotifications(in RxBusDriver rxBus, in String userId);
}
