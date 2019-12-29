package com.chanse.messaging.messages;

import com.chanse.messaging.utils.SaveLoadUtils;
import com.chanse.messaging.words.InterfaceDataWord;
import com.google.gson.*;
import lombok.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * The InterfaceMessage class represents the most basic interface message which is a compilation of "Fields"
 * which make up "words" that can be flattened into json for saving at a later point and serialized into
 * binary as well.
 *
 * This is just the abstract class that can be re-used later by more specific classes such as "Repeating Messages"
 * "protobuffer messages" "1553 messages" etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class InterfaceMessage{

    // Each message will hold a static reference to memory so that it will only calculate the given binary string
    // if a field has been updated on it.
    protected StringBuffer messageAsSerialString = new StringBuffer();

    // What do we call this message
    protected String messageName = new String("");

    // Flag to decide if it needs to recalculate the message to serial string
    protected transient boolean dataHasChanged;

    // The Individual Data Words because were cool like that
    protected List<InterfaceDataWord> dataWords = new ArrayList<>();

    /**
     * Method each message type will need to implement for how to find out its binary string. Different message
     * will do this differently... ie a protobuffer vs something that is simulating interlaced video.... SPOOOOKY
     */
    public abstract void recalculateMessageAsBinaryString();

    /**
     * Method to initially calculate the Binary String for a message.
     * Different from recalculateMessageAsBinaryString because this forcefully re-writes the entire binary string
     * for all fields regardless if they changed
     *
     * NOTE: Much Slower than above method.
     */
    public abstract void initializeMessageBinaryString();

    public void addDataWord(InterfaceDataWord dataWord){
        this.dataWords.add(dataWord);
        dataWord.addDataListener(myDataChangeListener);
    }
    public void removeDataWord(InterfaceDataWord dataWord){
        this.dataWords.remove(dataWord);
        dataWord.addDataListener(myDataChangeListener);
    }

    @Setter(AccessLevel.NONE)
    protected PropertyChangeListener myDataChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if(propertyChangeEvent.getSource() instanceof InterfaceDataWord){
                dataHasChanged = true;
            }
        }
    };

    public static class InterfaceMessageSerializer implements JsonSerializer<InterfaceMessage>{
        @Override
        public JsonElement serialize(InterfaceMessage src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = (JsonObject)(SaveLoadUtils.defaultGson.toJsonTree(src));
            object.addProperty("myClass", src.getClass().getName());
            return object;
        }
    }

    public static class InterfaceMessageDeserializer implements JsonDeserializer<InterfaceMessage>{
        @Override
        public InterfaceMessage deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            System.out.println("Here");
            return SaveLoadUtils.defaultGson.fromJson(json, typeOfT);
        }
    }
}
