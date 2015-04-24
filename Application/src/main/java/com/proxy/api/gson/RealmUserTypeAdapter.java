package com.proxy.api.gson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.proxy.api.domain.model.User;
import com.proxy.api.domain.realm.RealmChannel;
import com.proxy.api.domain.realm.RealmContact;
import com.proxy.api.domain.realm.RealmGroup;
import com.proxy.api.domain.realm.RealmUser;

import java.io.IOException;

import io.realm.RealmList;


/**
 * {@link User} TypeAdapter that deserializes JSON to create a user.
 */
@SuppressWarnings("all")
public class RealmUserTypeAdapter extends com.google.gson.TypeAdapter<RealmUser> {

    @Override
    public void write(JsonWriter out, RealmUser value) throws IOException {

    }

    @Override
    public RealmUser read(JsonReader reader) throws IOException {
        RealmUser user = new RealmUser();

        RealmList<RealmChannel> channels = new RealmList<>();
        RealmList<RealmContact> contacts = new RealmList<>();
        RealmList<RealmGroup> groups = new RealmList<>();

        while (reader.hasNext()) {

            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
            } else {
                reader.beginObject();
            }

            switch (reader.nextName()) {
                case "userId":
                    user.setUserId(reader.nextString());
                    break;
                case "firstName":
                    user.setFirstName(reader.nextString());
                    break;
                case "lastName":
                    user.setLastName(reader.nextString());
                    break;
                case "email":
                    user.setEmail(reader.nextString());
                    break;
                case "imageURL":
                    user.setImageURL(reader.nextString());
                    break;
                case "channels":
                    channels.add(readChannel(reader));
                    break;
                case "contacts":
                    contacts.add(readContact(reader));
                    break;
                case "groups":
                    groups.add(readGroup(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        reader.close();
        user.setChannels(channels);
        user.setContacts(contacts);
        user.setGroups(groups);
        return user;
    }

    /**
     * Parse a Channel.
     *
     * @param reader JSON
     * @return User Channel
     * @throws IOException cant read
     */
    public RealmChannel readChannel(JsonReader reader) throws IOException {
        RealmChannel channel = new RealmChannel();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "channelId":
                    channel.setChannelId(reader.nextString());
                    break;
                case "label":
                    channel.setLabel(reader.nextString());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return channel;
    }

    /**
     * Parse a Contact.
     *
     * @param reader JSON
     * @return User Contact
     * @throws IOException cant read
     */
    public RealmContact readContact(JsonReader reader) throws IOException {
        RealmContact contact = new RealmContact();
        RealmList<RealmChannel> channels = new RealmList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "contactId":
                    contact.setContactId(reader.nextString());
                    break;
                case "label":
                    contact.setLabel(reader.nextString());
                    break;
                case "channels":
                    channels.add(readChannel(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        contact.setChannels(channels);
        return contact;
    }

    /**
     * Parse a Group.
     *
     * @param reader JSON
     * @return User Group
     * @throws IOException cant read
     */
    public RealmGroup readGroup(JsonReader reader) throws IOException {
        RealmGroup group = new RealmGroup();
        RealmList<RealmChannel> channels = new RealmList<>();
        RealmList<RealmContact> contacts = new RealmList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "groupId":
                    group.setGroupId(reader.nextString());
                    break;
                case "label":
                    group.setLabel(reader.nextString());
                    break;
                case "channels":
                    channels.add(readChannel(reader));
                    break;
                case "contacts":
                    contacts.add(readContact(reader));
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        group.setChannels(channels);
        group.setContacts(contacts);
        return group;
    }
}
