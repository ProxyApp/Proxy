package com.shareyourproxy.api.rx.command

import android.content.Context
import com.shareyourproxy.api.rx.command.eventcallback.EventCallback

/**
 * Command Pattern.
 */
internal interface ExecuteCommand {
    fun execute(context: Context): EventCallback
}
