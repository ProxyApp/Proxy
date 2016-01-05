package com.shareyourproxy.api.domain.realm

import com.shareyourproxy.api.domain.model.ChannelType
import io.realm.RealmObject
import io.realm.annotations.RealmClass

/**
 * A Channels Type, Twitter, Facebook, Google Plus.
 */
@RealmClass
open class RealmChannelType(open var weight: Int = ChannelType.Custom.ordinal, open var label: String = ChannelType.Custom.label) : RealmObject()