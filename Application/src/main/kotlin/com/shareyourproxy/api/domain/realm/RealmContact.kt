package com.shareyourproxy.api.domain.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Contacts are [RealmUser]s who you'd like to communicate with.
 */
@RealmClass
open class RealmContact(
        @PrimaryKey open var id: String = "",
        open var first: String = "",
        open var last: String = "",
        open var profileURL: String = "",
        open var coverURL: String = "",
        open var channels: RealmList<RealmString>? = null) : RealmObject()
