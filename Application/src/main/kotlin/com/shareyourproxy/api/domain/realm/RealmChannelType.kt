package com.shareyourproxy.api.domain.realm

import com.shareyourproxy.api.domain.model.ChannelType
import io.realm.RealmObject
import io.realm.annotations.RealmClass

/**
 * Created by Evan on 5/4/15.
 */
@RealmClass
public open class RealmChannelType(public open var weight: Int = ChannelType.Custom.ordinal, public open var label: String = ChannelType.Custom.label) : RealmObject()