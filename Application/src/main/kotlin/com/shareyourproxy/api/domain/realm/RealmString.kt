package com.shareyourproxy.api.domain.realm

import io.realm.RealmObject
import io.realm.annotations.RealmClass

/**
 * String value
 */
@RealmClass
open class RealmString(open var value: String = "") : RealmObject()
