package com.shareyourproxy.api.rx.event

import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User

/**
 * Created by Evan on 11/9/15.
 */
internal final class ChannelAddedEvent(val user: User, val channel: Channel)
