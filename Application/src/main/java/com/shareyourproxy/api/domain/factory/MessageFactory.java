package com.shareyourproxy.api.domain.factory;

import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.Message;
import com.shareyourproxy.api.domain.realm.RealmMessage;

import java.util.HashMap;

import io.realm.RealmList;

import static com.shareyourproxy.api.domain.factory.ContactFactory.createModelContact;

/**
 * Created by Evan on 7/6/15.
 */
public class MessageFactory {
    public static HashMap<String, Message> getRealmMessages(RealmList<RealmMessage> messages) {
        HashMap<String, Message> userMessages = new HashMap<>(messages.size());
        for (RealmMessage message : messages) {
            Message newMessage =
                Message.create(Id.builder().value(message.getId()).build(),
                    createModelContact(message.getContact()));
            userMessages.put(message.getId(), newMessage);
        }
        return userMessages;
    }
}
