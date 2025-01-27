package com.chanse.messaging.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public interface MessagingSaveable {

    default String getMyClassName() {
        return this.getClass().getName();
    }

    class MessageSaveableAdapter implements JsonDeserializer<MessagingSaveable>, JsonSerializer<MessagingSaveable> {
        @Override
        public MessagingSaveable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                Class messageType = Class.forName(((JsonObject) json).get("myClassName").getAsString());

                return (MessagingSaveable)SaveLoadUtils.Instance.myRegisteredGson.fromJson(json, messageType);
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        @Override
        public JsonElement serialize(MessagingSaveable src, Type typeOfSrc, JsonSerializationContext context) {
            try {
                Type childType = TypeToken.get(Class.forName(src.getMyClassName())).getType();
                JsonObject objectJson = (JsonObject)SaveLoadUtils.Instance.myRegisteredGson.toJsonTree(src, childType);
                objectJson.addProperty("myClassName", src.getMyClassName());
                return objectJson;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
