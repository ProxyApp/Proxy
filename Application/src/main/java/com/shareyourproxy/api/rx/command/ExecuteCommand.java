package com.shareyourproxy.api.rx.command;

import android.app.IntentService;

import com.shareyourproxy.api.rx.command.event.CommandEvent;

import java.util.List;

/**
 * Command Pattern.
 */
public interface ExecuteCommand {
    List<CommandEvent> execute(IntentService service);
}
