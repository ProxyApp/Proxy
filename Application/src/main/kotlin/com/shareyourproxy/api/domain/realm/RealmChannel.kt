package com.shareyourproxy.api.domain.realm

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Channels are other apps and services that you will use to communicate with [RealmContact]s.
 */
@RealmClass
public open class RealmChannel(
        @PrimaryKey public open var id: String = "",
        public open var label: String = "",
        public open var actionAddress: String = "",
        public open var channelType: RealmChannelType = RealmChannelType(),
        public open var isPublic: Boolean = false) : RealmObject()
