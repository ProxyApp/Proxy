package com.shareyourproxy.api.domain.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Groups only have names for now.
 */
@RealmClass
open class RealmGroup(
        @PrimaryKey open var id: String = "",
        open var label: String = "",
        open var channels: RealmList<RealmString> = RealmList(),
        open var contacts: RealmList<RealmString> = RealmList()) : RealmObject()
