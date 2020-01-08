package com.chanse.messaging.utils;

import com.chanse.messaging.messages.InterfaceMessage;
import com.google.gson.*;
import jdk.nashorn.internal.objects.annotations.Getter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Type;

public interface MessagingSaveable {

    default String getMyClassName() {
        return this.getClass().getName();
    }

    public static class MessageSaveableDeserializer implements JsonDeserializer<MessagingSaveable> {
        @Override
        public MessagingSaveable deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            try {
                Class messageType = Class.forName(((JsonObject) json).get("myClassName").getAsString());
                return (MessagingSaveable)SaveLoadUtils.myGson.fromJson(json, messageType);
            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }
    }

    public static class MessagesSaveableSerializer implements JsonSerializer<MessagingSaveable> {

        @Override
        public JsonElement serialize(MessagingSaveable src, Type typeOfSrc, JsonSerializationContext context) {
            JsonElement objectJson = SaveLoadUtils.myGson.toJsonTree(src, typeOfSrc);
            objectJson.
            return null;
        }
    }

}
