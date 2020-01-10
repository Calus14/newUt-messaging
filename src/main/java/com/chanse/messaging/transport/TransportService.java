package com.chanse.messaging.transport;

import com.chanse.messaging.exceptions.ConnectionException;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.msginterface.InterfaceDecoder;

import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

/**
 * The Transport Service is an abstract class that creates a contract for other service to use.
 *
 * Example of transport services will be thing like TCP/UDP, Serial, Http, AMQP etc.
 */
public interface TransportService {

    enum Statuses{
        NOTHING,
        SETTING_UP,
        FAILED_SETUP,
        SET_UP,
        CONNECTING,
        FAILED_CONNECTION,
        CONNECTED,
        DISCONNECTED
    }

    default String getMyClassName(){
        return this.getClass().getName();
    }

    /**
     * If this transport Service needs the ability to decode then this method allows you to register one too it
     * @param decoder the decoder you wish to set on this interface
     */
    void setDecoder(InterfaceDecoder decoder);

    /**
     * Utility method to get the decoder if any
     */
    InterfaceDecoder getDecoder();

    /**
     * Method that will send the "msg" as bytes over whatever transport service is chosen.
     * NOTE: The message can be ill formed, empty, or giberish, the transport service is only focused
     * on sending the message
     * @param msg Byte representation of the message to be sent out
     */
    void sendMessage(byte[] msg);

    /**
     * Recieves all messages on this transport services input port as bytes. After attempting to decode
     * these messages any message that the decoder for this service cannot decode will be dropped and logged
     * @return a list of all decoded messages on this transport services inputStream
     */
    List<InterfaceMessage> receiveMessages();

    /**
     * Utility check to see if the system is still setup
     * @return if the service was set up correctly
     */
    boolean isSetup();

    /**
     * Method  that will attempt to do everything to set up the service except for start the connecting process
     */
    void setup();

    /**
     * Utility check to see if the service has sucessfully connected and still is
     * @return if the service is currently connected
     */
    boolean isConnected();

    /**
     * Connects the transport service in whatever protocol it requires and ends after the connection has been
     * confirmed if the connection is stateful
     * @throws ConnectionException Throws a connection exception when an error occurs in statefull connection handshakes
     */
    void connect() throws ConnectionException;

    /**
     * Disconnects the service from whatever protocol it requires
     * @throws IOException Thrown if there was some error with cleanly disconnecting this service
     */
    void disconnect() throws IOException;

    /**
     * Utility method to tell what state the transportation service is in.
     * @return State of connection for the service
     */
    String getStatus();

    class TransportServiceAdapter implements JsonSerializer<TransportService>, JsonDeserializer<TransportService>{

        @Override
        public TransportService deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = (JsonObject)json;

            // We need to specifically get the decoder and call into the Messaging utils so we cant let base Gson handle it
            JsonObject decoderJsonObject = null;
            if(jsonObject.get("decoder") != null) {
                decoderJsonObject = (JsonObject)jsonObject.remove("decoder");
            }

            TransportService transportService = null;
            try {
                Class messageType = Class.forName(jsonObject.get("myClassName").getAsString());
                transportService = (TransportService)SaveLoadUtils.Instance.defaultGson.fromJson(jsonObject, messageType);

                if(decoderJsonObject != null) {
                    // Method returns all decoders in the string, so their may be 0 - N
                    List<InterfaceDecoder> decoders =
                            SaveLoadUtils.Instance.loadInterfaceDecodersFromString(decoderJsonObject.toString());
                    if (decoders.size() == 0)
                        throw new IOException("Failed to load decoder successfully for Transport Service From Json");
                    transportService.setDecoder(decoders.get(0));
                }
            }
            catch(Exception e){
                // TODO handle exception
                e.printStackTrace();
            }

            return transportService;
        }

        @Override
        public JsonElement serialize(TransportService src, Type typeOfSrc, JsonSerializationContext context){
            try {
                Type childType = TypeToken.get(Class.forName(src.getMyClassName())).getType();
                JsonObject json = (JsonObject) SaveLoadUtils.Instance.myRegisteredGson.toJsonTree(src, childType);
                json.addProperty("myClassName", src.getClass().getName());

                // Specifically have to call into Messaging Save Load to get it to save and load the decoder correctly
                json.remove("decoder");

                JsonElement decoderElement = SaveLoadUtils.jsonParser.parse(
                                        SaveLoadUtils.Instance.getInterfaceDecoderSaveString(src.getDecoder()) );

                json.add("decoder", decoderElement );
                return json;
            }
            catch(ClassNotFoundException e){
                //TODO Handle exception
                e.printStackTrace();
                return null;
            }
        }
    }
}
