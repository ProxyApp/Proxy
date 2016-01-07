package com.shareyourproxy.api.domain.realm


import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Users have a basic profile that contains their specific [RealmChannel]s, [RealmContact]s, and [RealmGroup]s.
 */
@RealmClass open class RealmUser(
        @PrimaryKey open var id: String = "",
        open var first: String = "",
        open var last: String = "",
        open var fullName: String = "",
        open var email: String = "",
        open var profileURL: String = "",
        open var coverURL: String = "",
        open var channels: RealmList<RealmChannel> = RealmList(),
        open var contacts: RealmList<RealmString> = RealmList(),
        open var groups: RealmList<RealmGroup> = RealmList(),
        open var version: Int = 0) : RealmObject()
