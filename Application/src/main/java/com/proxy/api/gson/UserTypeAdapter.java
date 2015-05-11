package com.proxy.api.gson;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.proxy.api.domain.model.Channel;
import com.proxy.api.domain.model.ChannelSection;
import com.proxy.api.domain.model.ChannelType;
import com.proxy.api.domain.model.Contact;
import com.proxy.api.domain.model.Group;
import com.proxy.api.domain.model.User;

import java.io.IOException;
import java.util.ArrayList;

import timber.log.Timber;


/**
 * {@link User} TypeAdapter that deserializes JSON to create a user.
 */
@SuppressWarnings("all")
public class UserTypeAdapter extends com.google.gson.TypeAdapter<User> {

    private Gson gson;

    private UserTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    public static UserTypeAdapter newInstace() {
        return new UserTypeAdapter(new Gson());
    }

    @Override
    public void write(JsonWriter out, User value) throws IOException {
        AutoGson annotation = User.class.getAnnotation(AutoGson.class);
        gson.getAdapter(annotation.autoValueClass()).write(out, value);
    }

    @Override
    public User read(JsonReader reader) throws IOException {
        User.Builder user = User.builder();
        ArrayList<Channel> channels = new ArrayList<>();
        ArrayList<Contact> contacts = new ArrayList<>();
        ArrayList<Group> groups = new ArrayList<>();
        while (reader.hasNext()) {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
            } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case "userId":
                            user.userId(reader.nextString());
                            break;
                        case "firstName":
                            user.firstName(reader.nextString());
                            break;
                        case "lastName":
                            user.lastName(reader.nextString());
                            break;
                        case "email":
                            user.email(reader.nextString());
                            break;
                        case "imageURL":
                            user.imageURL(reader.nextString());
                            break;
                        case "channels":
                            checkChannelArray(reader, channels);
                            break;
                        case "contacts":
                            checkContactsArray(reader, contacts);
                        case "groups":
                            checkGroupsArray(reader, groups);
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                }
                reader.endObject();
                user.channels(channels);
                user.contacts(contacts);
                user.groups(groups);
                return user.build();
            } else if (reader.peek() == JsonToken.END_DOCUMENT) {
                return null;
            }
        }
        return null;
    }

    /**
     * Parse a Channel.
     *
     * @param reader JSON
     * @return User Channel
     * @throws IOException cant read
     */
    public Channel readChannel(JsonReader reader) throws IOException {
        Channel.Builder channel = Channel.builder();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "channelId":
                    channel.channelId(reader.nextString());
                    break;
                case "label":
                    channel.label(reader.nextString());
                    break;
                case "packageName":
                    channel.packageName(reader.nextString());
                    break;
                case "channelSection":
                    channel.channelSection(ChannelSection.valueOf(reader.nextString()));
                    break;
                case "channelType":
                    channel.channelType(ChannelType.valueOf(reader.nextString()));
                    break;
                case "actionAddress":
                    channel.actionAddress(reader.nextString());
                    break;
                default:
                    Timber.e("Skipped: " + reader.getPath());
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return channel.build();
    }

    /**
     * Parse a Contact.
     *
     * @param reader JSON
     * @return User Contact
     * @throws IOException cant read
     */
    public Contact readContact(JsonReader reader) throws IOException {
        Contact.Builder contact = Contact.builder();
        ArrayList<Channel> channels = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "contactId":
                    contact.contactId(reader.nextString());
                    break;
                case "label":
                    contact.label(reader.nextString());
                    break;
                case "channels":
                    checkChannelArray(reader, channels);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        contact.channels(channels);
        return contact.build();
    }

    /**
     * Parse a Group.
     *
     * @param reader JSON
     * @return User Group
     * @throws IOException cant read
     */
    public Group readGroup(JsonReader reader) throws IOException {
        Group.Builder group = Group.builder();
        ArrayList<Channel> channels = new ArrayList<>();
        ArrayList<Contact> contacts = new ArrayList<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "groupId":
                    group.groupId(reader.nextString());
                    break;
                case "label":
                    group.label(reader.nextString());
                    break;
                case "channels":
                    checkChannelArray(reader, channels);
                    break;
                case "contacts":
                    checkContactsArray(reader, contacts);
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        group.channels(channels);
        group.contacts(contacts);
        return group.build();
    }

    private void checkChannelArray(JsonReader reader, ArrayList<Channel> channels) throws
        IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                reader.nextName();
                channels.add(readChannel(reader));
            }
            reader.endObject();
        } else {
            Timber.e("checkChannelArray error");
        }
    }

    private void checkContactsArray(JsonReader reader, ArrayList<Contact> contacts) throws
        IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                reader.nextName();
                contacts.add(readContact(reader));
            }
            reader.endObject();
        } else {
            Timber.e("checkContactArray error");
        }
        return;
    }

    private void checkGroupsArray(JsonReader reader, ArrayList<Group> groups) throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                reader.nextName();
                groups.add(readGroup(reader));
            }
            reader.endObject();
        } else {

        }
    }
}
