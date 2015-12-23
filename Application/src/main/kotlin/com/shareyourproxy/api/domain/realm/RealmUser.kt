package com.shareyourproxy.api.domain.realm


import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Users have a basic profile that contains their specific [RealmChannel]s, [RealmContact]s, and [RealmGroup]s.
 */
@RealmClass
public open class RealmUser(
        @PrimaryKey public open var id: String = "",
        public open var first: String = "",
        public open var last: String = "",
        public open var fullName: String = "",
        public open var email: String = "",
        public open var profileURL: String = "",
        public open var coverURL: String = "",
        public open var channels: RealmList<RealmChannel> = RealmList(),
        public open var contacts: RealmList<RealmString> = RealmList(),
        public open var groups: RealmList<RealmGroup> = RealmList(),
        public open var version: Int = 0) : RealmObject()
