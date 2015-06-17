package com.shareyourproxy.api.rx.command;

import android.app.Service;

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback;

import java.util.List;

/**
 * Command Pattern.
 */
public interface ExecuteCommand {
    List<EventCallback> execute(Service service);
}
