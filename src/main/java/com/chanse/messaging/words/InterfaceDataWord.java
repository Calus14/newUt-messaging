package com.chanse.messaging.words;

import com.chanse.messaging.exceptions.BadFieldWriteException;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.fields.StaticDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.*;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.stereotype.Service;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Data Word in the Command and Control tech space is a concept which says there is a standard length of bytes
 * that makes up a word. A Word consists of multiple fields. and based on some identifying fields this is how you
 * determine how to decode the following data.
 *
 * IE A 10 byte word may decode into 8 8 byte fields if the first 2 bytes the word evaluate to 0 and 4
 * but instead it may become 16 4 bit fields if it is 0 and 5 etc.
 *
 * Again this concept is abstract because different industries use different types of "words" with slight variations.
 * Some are bit terminating and some are abstract sizes.
 */
@Data
@NoArgsConstructor
public abstract class InterfaceDataWord {

    // All of the words possible fields are stored here, it is up to the message to figure out how to go about handling
    // Which word is in which order. This should be done from the message configuration Service
    protected List<InterfaceDataField> dataFields = new ArrayList<>();

    // How many bytes is the word in total
    protected int numberOfBytes;

    // Statically hold in memory how to represent this word as bytes. This way we only recalcuate when we want to
    protected transient String wordDataAsBinaryString = new String("");

    // Flag to tell if any data on the word has changed
    protected transient boolean fieldDataHasChanged = false;

    // Name of the word for identifying it later on
    protected String wordName = new String("");

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected transient List<PropertyChangeListener> dataChangeListeners = new ArrayList<>();

    // Whenever a field changes we should know bout it so we can update just the one and be more effecient
    protected transient Set<InterfaceDataField> changedFields = new HashSet<>();

    @Setter(AccessLevel.NONE)
    protected PropertyChangeListener myFieldListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if(propertyChangeEvent.getSource() instanceof InterfaceDataField){
                fieldDataHasChanged = true;
                if (!changedFields.contains((InterfaceDataField)propertyChangeEvent.getSource())){
                    changedFields.add((InterfaceDataField)propertyChangeEvent.getSource());
                }
            }
        }
    };

    public void addDataField(InterfaceDataField dataField){
        this.dataFields.add(dataField);
        dataField.addFieldListener(myFieldListener);
    }

    public void removeDataField(InterfaceDataField dataField){
        this.dataFields.remove(dataField);
        dataField.removeFieldListener(myFieldListener);
    }

    public void addDataListener(PropertyChangeListener changeListener){
        this.dataChangeListeners.add(changeListener);
    }
    public void removeDataListener(PropertyChangeListener changeListener){
        this.dataChangeListeners.remove(changeListener);
    }

    // Method that should run whenever trying to write a word that has had data on it change
    // Being included on the Data object because we want to have the data knowledgeable of itself as bytes
    public abstract void updateChangedFields() throws BadFieldWriteException;

    public static class InterfaceDataWordSerializer implements JsonSerializer<InterfaceDataWord> {
        @Override
        public JsonElement serialize(InterfaceDataWord src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject object = (JsonObject)(SaveLoadUtils.defaultGson.toJsonTree(src));
            object.addProperty("myClass", src.getClass().getName());
            return object;
        }
    }
}
