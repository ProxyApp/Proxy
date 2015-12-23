package com.shareyourproxy.api.domain.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Groups only have names for now.
 */
@RealmClass
public open class RealmGroup(
        @PrimaryKey public open var id: String = "",
        public open var label: String = "",
        public open var channels: RealmList<RealmString> = RealmList(),
        public open var contacts: RealmList<RealmString> = RealmList()) : RealmObject()
