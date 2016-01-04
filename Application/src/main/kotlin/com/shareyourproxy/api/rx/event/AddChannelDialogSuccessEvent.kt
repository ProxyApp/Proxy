package com.shareyourproxy.api.rx.event

import com.shareyourproxy.api.domain.model.Channel
import com.shareyourproxy.api.domain.model.User

/**
 * Created by Evan on 11/17/15.
 */
internal final class AddChannelDialogSuccessEvent(val user: User, val channel: Channel)
