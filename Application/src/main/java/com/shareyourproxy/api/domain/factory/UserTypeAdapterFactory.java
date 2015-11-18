package com.shareyourproxy.api.domain.factory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.shareyourproxy.api.domain.model.User;

import static com.shareyourproxy.util.ObjectUtils.buildFullName;

/**
 * Created by Evan on 11/3/15.
 */
public class UserTypeAdapterFactory extends CustomTypeAdapterFactory<User> {
    public UserTypeAdapterFactory() {
        super(User.class);
    }

    @Override
    protected void beforeWrite(User source, JsonElement toSerialize) {
        JsonObject custom = toSerialize.getAsJsonObject();
        custom.remove("fullName");
    }

    @Override
    protected void afterRead(JsonElement deserialized) {
        if (deserialized.isJsonObject()) {
            JsonObject obj = deserialized.getAsJsonObject();
            JsonElement firstName = obj.get("first");
            JsonElement lastName = obj.get("last");
            String first = firstName == null ? "" : firstName.getAsString();
            String last = lastName == null ? "" : lastName.getAsString();
            obj.add("fullName", new JsonPrimitive(buildFullName(first, last)));
        }
    }
}
