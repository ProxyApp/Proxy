package com.shareyourproxy.api.domain.realm;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * RealmMessage to save user notifications
 */
public class RealmMessage extends RealmObject {

    @PrimaryKey
    private String id;
    private RealmContact contact;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RealmContact getContact() {
        return contact;
    }

    public void setContact(RealmContact contact) {
        this.contact = contact;
    }
}
