package com.shareyourproxy.api.rx.command

import android.app.Service

import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Command Pattern.
 */
interface ExecuteCommand {
    fun execute(service: Service): EventCallback
}
