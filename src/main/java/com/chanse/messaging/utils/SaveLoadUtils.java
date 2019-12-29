package com.chanse.messaging.utils;

import com.chanse.messaging.fields.EnumDataField;
import com.chanse.messaging.fields.IntegerDataField;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.fields.StaticDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.messages.StandardMessage;
import com.chanse.messaging.words.InterfaceDataWord;
import com.chanse.messaging.words.StandardDataWord;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonStreamParser;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility namespace for how to save and load anything inside of the messaging service/module
 * Specifically will implement strategy for saveing and loading a lamda etc.
 */
public class SaveLoadUtils {

    // A standard gson object so we can do standard serialization and add properties or manipulate properties later
    // there is probabaly a better way to do this...
    public static final Gson defaultGson = new Gson();

    // The Custom Gson mapper that will be used by this actual service
    public static Gson myGson;

    public static SaveLoadUtils Instance = new SaveLoadUtils();

    private SaveLoadUtils(){
        myGson = new GsonBuilder()
                // DESERIALIZERS
                // Messages
                .registerTypeAdapter(InterfaceMessage.class, new InterfaceMessage.InterfaceMessageDeserializer())
                // Words
                .registerTypeAdapter(InterfaceDataWord.class, new InterfaceDataWord.InterfaceDataWordDeserializer())
                //Fields
                .registerTypeAdapter(InterfaceDataField.class, new InterfaceDataField.InterfaceDataFieldDeserializer())
                .setPrettyPrinting()
                .create();
    }

    public String getMessageSaveString(InterfaceMessage msg){
        String answer = myGson.toJson(msg);
        JsonElement ele = myGson.toJsonTree(msg);
        System.out.println(ele);
        return answer;
    }

    public List<InterfaceMessage> loadMessageFromSaveString(String loadString) throws IOException {
        List<InterfaceMessage> loadedMessages = new ArrayList<>();

        StringReader reader = new StringReader(loadString);
        JsonStreamParser p = new JsonStreamParser(reader);
        while (p.hasNext()) {
            JsonElement e = p.next();
            if (e.isJsonObject()) {
                InterfaceMessage message = myGson.fromJson(e, InterfaceMessage.class);
                loadedMessages.add(message);
            }
        }
        reader.close();

        return loadedMessages;
    }

    public List<InterfaceMessage> loadMessagesFromFile(String filePath) throws FileNotFoundException, IOException {
        List<InterfaceMessage> loadedMessages = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(("src/test/resources/testSaveFile1.json")));
        JsonStreamParser p = new JsonStreamParser(reader);
        while (p.hasNext()) {
            JsonElement e = p.next();
            if (e.isJsonObject()) {
                InterfaceMessage message = myGson.fromJson(e, InterfaceMessage.class);
                loadedMessages.add(message);
            }
        }
        reader.close();

        return loadedMessages;
    }

}
