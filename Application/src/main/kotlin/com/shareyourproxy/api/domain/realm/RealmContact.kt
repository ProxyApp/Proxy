package com.shareyourproxy.api.domain.realm

import io.realm.RealmList
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Contacts are [RealmUser]s who you'd like to communicate with.
 */
@RealmClass
public open class RealmContact(
        @PrimaryKey public open var id: String = "",
        public open var first: String = "",
        public open var last: String = "",
        public open var profileURL: String = "",
        public open var coverURL: String = "",
        public open var channels: RealmList<RealmString>? = null) : RealmObject()
