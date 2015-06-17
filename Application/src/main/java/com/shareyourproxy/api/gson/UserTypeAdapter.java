package com.shareyourproxy.api.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelSection;
import com.shareyourproxy.api.domain.model.ChannelType;
import com.shareyourproxy.api.domain.model.Contact;
import com.shareyourproxy.api.domain.model.Group;
import com.shareyourproxy.api.domain.model.Id;
import com.shareyourproxy.api.domain.model.User;

import java.io.IOException;
import java.util.HashMap;

import timber.log.Timber;


/**
 * {@link User} TypeAdapter that deserializes JSON to create a user.
 */
public class UserTypeAdapter extends TypeAdapter<User> {

    private Gson gson;

    private UserTypeAdapter(Gson gson) {
        this.gson = gson;
    }

    public static UserTypeAdapter newInstace() {
        return new UserTypeAdapter(new Gson());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(JsonWriter out, User value) throws IOException {
        AutoGson annotation = User.class.getAnnotation(AutoGson.class);
        gson.getAdapter(annotation.autoValueClass()).write(out, value);
    }

    @Override
    public User read(JsonReader reader) throws IOException {
        User.Builder user = User.builder();
        HashMap<String, Channel> channels = new HashMap<>();
        HashMap<String, Contact> contacts = new HashMap<>();
        HashMap<String, Group> groups = new HashMap<>();
        while (reader.hasNext()) {
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
            } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                reader.beginObject();
                while (reader.hasNext()) {
                    switch (reader.nextName()) {
                        case "id":
                            user.id(readId(reader));
                            break;
                        case "first":
                            user.first(reader.nextString());
                            break;
                        case "last":
                            user.last(reader.nextString());
                            break;
                        case "email":
                            user.email(reader.nextString());
                            break;
                        case "profileURL":
                            user.profileURL(reader.nextString());
                            break;
                        case "coverURL":
                            user.coverURL(reader.nextString());
                            break;
                        case "channels":
                            checkChannelArray(reader, channels);
                            break;
                        case "contacts":
                            checkContactsArray(reader, contacts);
                            break;
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

    public Id readId(JsonReader reader) throws IOException {
        String res = null;
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                switch (reader.nextName()) {
                    case "value":
                        res = reader.nextString();
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }
            reader.endObject();
        } else if (reader.peek() == JsonToken.STRING) {
            res = reader.nextString();
        }

        if (res == null) {
            throw new IOException("Invalid Json");
        } else {
            return Id.builder().value(res).build();
        }
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
                case "id":
                    channel.id(readId(reader));
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
        HashMap<String, Channel> channels = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "id":
                    contact.id(readId(reader));
                    break;
                case "first":
                    contact.first(reader.nextString());
                    break;
                case "last":
                    contact.last(reader.nextString());
                    break;
                case "profileURL":
                    contact.profileURL(reader.nextString());
                    break;
                case "coverURL":
                    contact.coverURL(reader.nextString());
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
        HashMap<String, Channel> channels = new HashMap<>();
        HashMap<String, Contact> contacts = new HashMap<>();

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "id":
                    group.id(readId(reader));
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

    private void checkChannelArray(JsonReader reader, HashMap<String, Channel> channels)
        throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                channels.put(reader.nextName(), readChannel(reader));
            }
            reader.endObject();
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                Channel channel = readChannel(reader);
                channels.put(channel.id().value(), channel);
            }
            reader.endArray();
        } else {
            Timber.e("checkChannelArray onError");
            throw new IOException("Invalid Json");
        }
    }

    private void checkContactsArray(JsonReader reader, HashMap<String, Contact> contacts)
        throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                contacts.put(reader.nextName(), readContact(reader));
            }
            reader.endObject();
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {

                if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                    Contact contact = readContact(reader);
                    contacts.put(contact.id().value(), contact);
                } else {
                    contacts.put(reader.nextName(), readContact(reader));
                }
            }
            reader.endArray();
        } else {
            Timber.e("checkContactArray onError");
            throw new IOException("Invalid Json");
        }
    }

    private void checkGroupsArray(JsonReader reader, HashMap<String, Group> groups)
        throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                groups.put(reader.nextName(), readGroup(reader));
            }
            reader.endObject();
        } else if (reader.peek() == JsonToken.BEGIN_ARRAY) {
            reader.beginArray();
            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                    Group group = readGroup(reader);
                    groups.put(group.id().value(), group);
                } else {
                    groups.put(reader.nextName(), readGroup(reader));
                }
            }
            reader.endArray();
        } else {
            Timber.i("Reader Error: " + reader.peek().toString());
            Timber.e("checkGroupsArray onError");
            throw new IOException("Invalid Json");
        }
    }
}
