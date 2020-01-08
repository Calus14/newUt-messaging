package com.chanse.messaging.msginterface;

import com.chanse.messaging.exceptions.BaseMessagingException;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.utils.MessagingSaveable;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.chanse.messaging.words.InterfaceDataWord;
import com.google.gson.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Interface decoders are simple classes that are meant to be able to decode a data stream into messages
 * provide detailed errors if an error is reached, and be interchangeable allowing greater modularity
 *
 * Example decoders might be
 * - StandardMessageDecoder (Peak at a stream, look at the key bits to find how to decode the next message, and then decode it)
 * - String Message Decoder (A String in the first bytes of the object tells it what message it is)
 * - Http decoders (I'm more or less spit balling here)
 */
public abstract class InterfaceDecoder implements MessagingSaveable {

    @Getter
    @Setter
    transient protected InputStream inputStream;

    public InterfaceDecoder(InputStream inputStream){
        this.inputStream = inputStream;
    }

    // To avoid throwing exceptions we will just internally note if an error occurs.
    // Otherwise there is the case where 1 message in 10,000 cause the entire internal state to fail
    transient static boolean errorOccurred = false;
    transient static StringBuffer errorLog = new StringBuffer();

    public boolean hasErrorOccured(){
        return errorOccurred;
    }

    /**
     * Returns a string of all errors that have occured on this decoder. Then sets the error state to a non-error state
     * @return List of all errors that occured
     */
    public String getListOfAllErrors(){
        return errorLog.toString();
    }

    // These methods specifically do not throw exceptions because if we fail to read 1 message we can still try to
    // go on. It is on the users of these interfaces to read the errorLog\
    /**
     * Reads all messages on the contained InputBuffer and returns them.
     * THIS WILL FLUSH THE BUFFER
     * @return List of all messages that were contained on the buffer
     */
    public abstract List<InterfaceMessage> decodeMessages() throws IOException, BaseMessagingException;

    /**
     * Reads all messages up to the given number of messages from the buffer.
     * Flushes only up to what was read
     * @param maxMessages the number of maximum messages to read from the buffer
     * @return List of all messages that were contained on the buffer
     */
    public abstract List<InterfaceMessage> decodeMessages(int maxMessages) throws IOException, BaseMessagingException;

}
