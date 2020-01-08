package com.chanse.messaging.msginterface;

import com.chanse.messaging.exceptions.BaseMessagingException;
import com.chanse.messaging.utils.BitUtils;
import com.chanse.messaging.exceptions.DuplicateMessageIdException;
import com.chanse.messaging.exceptions.IdOverlapException;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.utils.MessagingSaveable;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.*;

/**
 * A Static ID Decoder is a decoder for an interface that has "Keys" in known locations at the start of every
 * message. This decoder will take the given stream, look for those keys, grab the tuple of said keys, and generate the message
 *
 * Static ID Decoders require that:
 * -The location of the ID fields are at set areas NOT DYNAMIC AREAS (Based off field 1 the next value will be located in a new position)
 * -The ID fields form a unique tuple of values on a Message by Message basis
 * -The ID Fields are set lenghts (Some interfaces say... a protobuffer) will have the ID start somewhere but be null terminated. and variable length
 * -The entire message is on the buffer all at once it cant be sent peicewise
 *
 * Static ID Decoders do not require messages to be fixed length.
 */
public class StaticIdDecoder extends InterfaceDecoder{

    public StaticIdDecoder(InputStream input){
        super(input);
    }
    public StaticIdDecoder(){ super(null); }

    /**
     * Internal helper class that lets us know how to peak at an input to stream to find an ID.
     */
    @Data
    @AllArgsConstructor
    public class IdPeekInfo{
        /**
         * How far down the stream of data so we start to peak
         */
        public int bitOffset;

        /**
         * How far down the stream of data do we read after starting
         */
        public int bitLength;
    }

    protected List<IdPeekInfo> idPeekInfoList = new ArrayList<>();
    protected Map<List<BigInteger>, InterfaceMessage> idToMessageMap = new HashMap<>();

    @Override
    public boolean equals(Object obj){
        if(obj instanceof StaticIdDecoder == false)
            return false;
        StaticIdDecoder other = (StaticIdDecoder)obj;

        if(idPeekInfoList.size() != other.idPeekInfoList.size())
            return false;
        for(int i = 0; i < idPeekInfoList.size(); i++){
            if(!idPeekInfoList.get(i).equals(other.idPeekInfoList.get(i)))
                return false;
        }

        if(idToMessageMap.size() != other.idToMessageMap.size())
            return false;
        for(Map.Entry e : idToMessageMap.entrySet()){
            if( !other.idToMessageMap.containsKey(e.getKey()) ||
                !other.idToMessageMap.get(e.getKey()).equals(e.getValue()) )
                return false;
        }

        return true;
    }

    public void addIdPeekInfo(int offset, int length) throws IdOverlapException {
        IdPeekInfo newPeekInfo = new IdPeekInfo(offset, length);
        idPeekInfoList.add(newPeekInfo);
        try{
            validateIdPeekInfo();
        }catch(IdOverlapException e){
            idPeekInfoList.remove(newPeekInfo);

            throw e;
        }
    }

    /**
     * Helper method that validates no ID info would be overlapping on other ID info.
     * IE start at bit 5 go for a length of 5 bits, start at bit 8 go for a length 8 bits colides on bit 8 9 10
     * @throws IdOverlapException if any of the ID's for this Interface would be overlapping
     */
    protected void validateIdPeekInfo() throws IdOverlapException{
        StringBuilder errorMessage = new StringBuilder();
        // Simple strategy that has a large memory cost. But because this will only be called at configuration
        // it doesnt matter
        Set<Integer> uniqueBits = new HashSet<Integer>();
        for(IdPeekInfo p : idPeekInfoList){
            for(int i=0; i<p.getBitLength(); i++){
                if(uniqueBits.contains(new Integer(i+p.getBitOffset())))
                    errorMessage.append("Bit "+(i+p.getBitOffset())+ " was found to overlap.");
                else
                    uniqueBits.add(new Integer(i+p.getBitOffset()));
            }
        }

        if(errorMessage.length() != 0)
            throw new IdOverlapException(errorMessage.toString());
    }

    public void addMessage(List<BigInteger> idValues, InterfaceMessage message) throws DuplicateMessageIdException {
        if (idPeekInfoList.size() != idValues.size()){
            throw new DuplicateMessageIdException("Tried to add message "+message.getMessageName()+" but only provided "+
                    idValues.size()+" ID's and the StaticIDDecoder expects "+idPeekInfoList.size()+" ID's. Unable to add message.");
        }
        else if(idToMessageMap.containsKey(idValues)) {
            throw new DuplicateMessageIdException("Error adding message " + message.getMessageName() + " the associated ID of:\n" + idValues.toString() +
                    "\nHas already been added to the StaticIdDecoder");
        }

        // Add a clone so that in the case the message is modifified by another service the copy doesnt change.
        idToMessageMap.put(idValues, message.clone());
    }

    @Override
    public List<InterfaceMessage> decodeMessages() throws IOException, BaseMessagingException {
        List<InterfaceMessage> decodedMessages = new ArrayList<InterfaceMessage>();

        byte[] dataToDecode = new byte[this.inputStream.available()];
        this.inputStream.read(dataToDecode);
        while(dataToDecode.length > 0){
                // Find what message to get
                List<BigInteger> messageUniqueId = new ArrayList<>();
                for(IdPeekInfo id : idPeekInfoList)
                    messageUniqueId.add(BitUtils.peekAtBits(id.bitOffset, id.bitLength, dataToDecode));

                if(!idToMessageMap.containsKey(messageUniqueId)) {
                    errorOccurred = true;
                    errorLog.append("Unable to find any message associated with the following ids:"+messageUniqueId+"\n Aborting reading any more messages with this call");
                    break;
                }

                // Get a clone so that it can be unique from other messages.
                InterfaceMessage message = idToMessageMap.get(messageUniqueId).clone();
                int messageBytes = BitUtils.fillMessageFromData(message, dataToDecode);
                decodedMessages.add(message);
                dataToDecode = Arrays.copyOfRange(dataToDecode, messageBytes, dataToDecode.length);
        }

        return decodedMessages;
    }

    @Override
    public List<InterfaceMessage> decodeMessages(int maxMessages)  throws IOException, BaseMessagingException {
        List<InterfaceMessage> decodedMessages = new ArrayList<InterfaceMessage>();

        byte[] dataToDecode = new byte[this.inputStream.available()];
        this.inputStream.read(dataToDecode);
        while(dataToDecode.length > 0 && decodedMessages.size() < maxMessages){
            // Find what message to get
            List<BigInteger> messageUniqueId = new ArrayList<>();
            for(IdPeekInfo id : idPeekInfoList)
                messageUniqueId.add(BitUtils.peekAtBits(id.bitOffset, id.bitLength, dataToDecode));

            if(!idToMessageMap.containsKey(messageUniqueId)) {
                errorOccurred = true;
                errorLog.append("Unable to find any message associated with the following ids:"+messageUniqueId+"\n Aborting reading any more messages with this call");
                break;
            }

            InterfaceMessage message = idToMessageMap.get(messageUniqueId);
            BitUtils.fillMessageFromData(message, dataToDecode);
            decodedMessages.add(message);
        }

        return decodedMessages;
    }

    public static class StaticIdDecoderAdapter implements JsonDeserializer<StaticIdDecoder> {
        static protected Type peekListType = new TypeToken<ArrayList<IdPeekInfo>>() {}.getType();
        static protected Type idMapType = new TypeToken<HashMap<String, InterfaceMessage>>() {}.getType();

        @Override
        public StaticIdDecoder deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {


            try {
                StaticIdDecoder decoder = new StaticIdDecoder();

                JsonObject decoderAsJSon = (JsonObject)json;
                JsonArray idPeekInfoJson = decoderAsJSon.getAsJsonArray("idPeekInfoList");
                decoder.idPeekInfoList = SaveLoadUtils.myRegisteredGson.fromJson(idPeekInfoJson, peekListType);

                JsonObject idToMessageMapJson = decoderAsJSon.getAsJsonObject("idToMessageMap");
                HashMap<String, InterfaceMessage> stringtoMessageMap =
                                        SaveLoadUtils.myRegisteredGson.fromJson(idPeekInfoJson, idMapType);
                /*
                Convert comma separated String to List

List<String> items = Arrays.asList(str.split("\\s*,\\s*"));
The above code splits the string on a delimiter defined as: zero or more whitespace, a literal comma, zero or more whitespace which will place the words into the list and collapse any whitespace between the words and commas.

Please note that this returns simply a wrapper on an array: you CANNOT for example .remove() from the resulting List. For an
                 */
                for(Map.Entry e : )

                //Type mapType = new TypeToken<HashMap<>>


                Class messageType = Class.forName(((JsonObject) json).get("myClassName").getAsString());

                return decoder;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }
}
