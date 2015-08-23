package com.shareyourproxy.api.gson;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.shareyourproxy.api.domain.model.Channel;
import com.shareyourproxy.api.domain.model.ChannelType;
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

    public static UserTypeAdapter newInstance() {
        return new UserTypeAdapter(new Gson());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(JsonWriter out, User value) throws IOException {
        Class annotation = User.class.getAnnotation(AutoGson.class).autoValueClass();
        gson.getAdapter(annotation).write(out, value);
    }

    @Override
    public User read(JsonReader reader) throws IOException {
        User.Builder user = User.builder();
        HashMap<String, Channel> channels = new HashMap<>();
        HashMap<String, Id> contacts = new HashMap<>();
        HashMap<String, Group> groups = new HashMap<>();
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.NAME) {
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
                            user.coverURL(readCoverURL(reader));
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
                        case "version":
                            user.version(reader.nextInt());
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                } else if (reader.peek() == JsonToken.STRING) {
                    Timber.e(reader.nextString());
                    user.coverURL(readCoverURL(reader));
                } else if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull();
                } else if (reader.peek() == JsonToken.END_DOCUMENT) {
                    return null;
                } else {
                    Timber.e("User read onError");
                    throw new IOException("Invalid Json");
                }
            }
            reader.endObject();
            user.channels(channels);
            user.contacts(contacts);
            user.groups(groups);
            return user.build();
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
        } else if (reader.peek() == JsonToken.NAME) {
            res = reader.nextName();
        }

        if (res == null) {
            throw new IOException("Invalid Json");
        } else {
            return Id.builder().value(res).build();
        }
    }

    public Id readContactId(JsonReader reader) throws IOException {
        String res = null;
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                    reader.beginObject();
                    switch (reader.nextName()) {
                        case "id":
                            res = readId(reader).value();
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                } else if (reader.peek() == JsonToken.NAME) {
                    switch (reader.nextName()) {
                        case "id":
                            res = readId(reader).value();
                            break;
                        case "value":
                            res = reader.nextString();
                            break;
                        default:
                            reader.skipValue();
                            break;
                    }
                }
            }
            reader.endObject();
        } else if (reader.peek() == JsonToken.STRING) {
            res = reader.nextString();
        } else if (reader.peek() == JsonToken.NAME) {
            res = reader.nextName();
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
            if (reader.peek() == JsonToken.NULL) {
                reader.nextNull();
            }
            switch (reader.nextName()) {
                case "id":
                    channel.id(readId(reader));
                    break;
                case "label":
                    channel.label(reader.nextString());
                    break;
                case "channelType":
                    channel.channelType(ChannelType.valueOf(reader.nextString()));
                    break;
                case "actionAddress":
                    channel.actionAddress(reader.nextString());
                    break;
                default:
                    reader.skipValue();
                    break;
            }
        }
        reader.endObject();
        return channel.build();
    }

    public String readCoverURL(JsonReader reader) throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            return reader.nextString();
        } else if (reader.peek() == JsonToken.STRING) {
            return reader.nextString();
        } else if (reader.peek() == JsonToken.NULL) {
            return null;
        } else {
            return null;
        }
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
        HashMap<String, Id> channels = new HashMap<>();
        HashMap<String, Id> contacts = new HashMap<>();

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
                    checkGroupChannelArray(reader, channels);
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

    private void checkGroupChannelArray(JsonReader reader, HashMap<String, Id> channels)
        throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                if (reader.peek() == JsonToken.NAME) {
                    String id = reader.nextName();
                    channels.put(id, Id.create(id));
                } else if (reader.peek() == JsonToken.BEGIN_OBJECT) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        switch (reader.nextName()) {
                            case "id":
                                String id = readId(reader).value();
                                channels.put(id, Id.create(id));
                                break;
                            default:
                                reader.skipValue();
                                break;
                        }
                    }
                    reader.endObject();
                }
            }
            reader.endObject();
        } else {
            Timber.e("checkChannelArray onError expected:" +
                reader.peek().toString());
            throw new IOException("Invalid Json");
        }
    }

    private void checkContactsArray(JsonReader reader, HashMap<String, Id> contacts)
        throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                Id id = readContactId(reader);
                contacts.put(id.value(), id);
            }
            reader.endObject();
        } else {
            Timber.e("checkContactArray onError");
            throw new IOException("Invalid Json");
        }
    }

    private void checkChannelArray(JsonReader reader, HashMap<String, Channel> channels)
        throws IOException {
        if (reader.peek() == JsonToken.BEGIN_OBJECT) {
            reader.beginObject();
            while (reader.hasNext()) {
                channels.put(reader.nextName(), readChannel(reader));
            }
            reader.endObject();
        } else {
            Timber.e("checkChannelArray onError expected:" +
                reader.peek().toString());
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
        } else {
            Timber.i("Reader Error: " + reader.peek().toString());
            Timber.e("checkGroupsArray onError");
            throw new IOException("Invalid Json");
        }
    }
}
