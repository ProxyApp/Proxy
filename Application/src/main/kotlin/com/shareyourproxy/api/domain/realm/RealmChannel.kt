package com.shareyourproxy.api.domain.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Channels are other apps and services that you will use to communicate with [RealmContact]s.
 */
@RealmClass
open class RealmChannel(
        @PrimaryKey open var id: String = "",
        open var label: String = "",
        open var actionAddress: String = "",
        open var channelType: RealmChannelType = RealmChannelType(),
        open var isPublic: Boolean = false) : RealmObject()
