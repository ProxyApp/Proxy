package com.shareyourproxy.api.domain.realm

import io.realm.RealmObject
import io.realm.annotations.RealmClass

/**
 * String value
 */
@RealmClass
public open class RealmString(public open var value: String = "") : RealmObject()
