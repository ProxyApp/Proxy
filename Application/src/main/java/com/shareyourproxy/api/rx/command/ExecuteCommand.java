package com.shareyourproxy.api.rx.command;

import android.app.Service;

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

/**
 * Command Pattern.
 */
public interface ExecuteCommand {
    EventCallback execute(Service service);
}
