package com.chanse.messaging.utils;

import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.msginterface.InterfaceDecoder;
import com.chanse.messaging.msginterface.StaticIdDecoder;
import com.chanse.messaging.words.InterfaceDataWord;
import com.google.gson.*;

import java.io.*;
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
    public static Gson myRegisteredGson;

    public static SaveLoadUtils Instance = new SaveLoadUtils();

    private SaveLoadUtils(){
        myRegisteredGson = new GsonBuilder()
                .registerTypeAdapter(InterfaceMessage.class, new MessagingSaveable.MessageSaveableAdapter())
                .registerTypeAdapter(InterfaceDataWord.class, new MessagingSaveable.MessageSaveableAdapter())
                .registerTypeAdapter(InterfaceDataField.class, new MessagingSaveable.MessageSaveableAdapter())
                .registerTypeAdapter(InterfaceDecoder.class, new MessagingSaveable.MessageSaveableAdapter())
                .registerTypeAdapter(StaticIdDecoder.class, new StaticIdDecoder.StaticIdDecoderAdapter())
                .setPrettyPrinting()
                .create();
    }

    public String getMessageSaveString(InterfaceMessage msg){
        JsonObject answerAsJson = (JsonObject)myRegisteredGson.toJsonTree(msg, InterfaceMessage.class);
        return myRegisteredGson.toJson(answerAsJson);
    }

    public String getInterfaceDecoderSaveString(InterfaceDecoder decoder){
        JsonObject answerAsJson = (JsonObject)myRegisteredGson.toJsonTree(decoder, InterfaceMessage.class);
        return myRegisteredGson.toJson(answerAsJson);
    }

    public List<InterfaceMessage> loadMessageFromSaveString(String loadString) throws IOException {
        List<InterfaceMessage> loadedMessages = new ArrayList<>();

        StringReader reader = new StringReader(loadString);
        JsonStreamParser p = new JsonStreamParser(reader);
        while (p.hasNext()) {
            JsonElement e = p.next();
            if (e.isJsonObject()) {
                InterfaceMessage message = myRegisteredGson.fromJson(e, InterfaceMessage.class);
                loadedMessages.add(message);
            }
        }
        reader.close();

        return loadedMessages;
    }

    public List<InterfaceMessage> loadMessagesFromFile(String filePath) throws FileNotFoundException, IOException {
        List<InterfaceMessage> loadedMessages = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader((filePath)));
        JsonStreamParser p = new JsonStreamParser(reader);
        while (p.hasNext()) {
            JsonElement e = p.next();
            if (e.isJsonObject()) {
                InterfaceMessage message = myRegisteredGson.fromJson(e, InterfaceMessage.class);
                loadedMessages.add(message);
            }
        }
        reader.close();

        return loadedMessages;
    }

    public List<InterfaceDecoder> loadInterfaceDecodersFromString(String loadString) throws IOException {
        List<InterfaceDecoder> loadedDecoders = new ArrayList<>();
        StringReader reader = new StringReader(loadString);
        JsonStreamParser p = new JsonStreamParser(reader);
        while (p.hasNext()) {
            JsonElement e = p.next();
            if (e.isJsonObject()) {
                InterfaceDecoder decoder = myRegisteredGson.fromJson(e, InterfaceDecoder.class);
                loadedDecoders.add(decoder);
            }
        }
        reader.close();

        return loadedDecoders;
    }

    public List<InterfaceDecoder> loadInterfaceDecodersFromFile(String filePath) throws FileNotFoundException, IOException {
        List<InterfaceDecoder> loadedDecoders = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader((filePath)));
        JsonStreamParser p = new JsonStreamParser(reader);
        while (p.hasNext()) {
            JsonElement e = p.next();
            if (e.isJsonObject()) {
                InterfaceDecoder decoder = myRegisteredGson.fromJson(e, InterfaceDecoder.class);
                loadedDecoders.add(decoder);
            }
        }
        reader.close();

        return loadedDecoders;
    }
}
