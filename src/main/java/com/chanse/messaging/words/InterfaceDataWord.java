package com.chanse.messaging.words;

import com.chanse.messaging.exceptions.BadFieldWriteException;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.fields.StaticDataField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Required;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
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
    protected List<InterfaceDataField> dataFields;

    // How many bytes is the word in total
    protected int numberOfBytes;

    // Statically hold in memory how to represent this word as bytes. This way we only recalcuate when we want to
    protected BigInteger wordData;

    // Flag to tell if any data on the word has changed
    protected boolean fieldDataHasChanged = false;

    // Name of the word for identifying it later on
    protected String wordName;

    // Whenever a field changes we should know bout it so we can update just the one and be more effecient
    protected Set<InterfaceDataField> changedFields = new HashSet<>();

    protected PropertyChangeListener myFieldListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if(propertyChangeEvent.getNewValue() instanceof InterfaceDataField){
                fieldDataHasChanged = true;
                if (changedFields.contains((InterfaceDataField)propertyChangeEvent.getNewValue())){
                    changedFields.add((InterfaceDataField)propertyChangeEvent.getNewValue());
                }
            }
        }
    };

    // Method that should run whenever trying to write a word that has had data on it change
    // Being included on the Data object because we want to have the data knowledgeable of itself as bytes
    public abstract void updateChangedFields() throws BadFieldWriteException;
}
