package com.chanse.messaging.words;

import com.chanse.messaging.exceptions.BadFieldWriteException;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.fields.StaticDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.utils.MessagingSaveable;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.*;
import lombok.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
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
public abstract class InterfaceDataWord implements Cloneable, MessagingSaveable{

    // All of the words possible fields are stored here, it is up to the message to figure out how to go about handling
    // Which word is in which order. This should be done from the message configuration Service
    protected List<InterfaceDataField> dataFields = new ArrayList<>();

    // How many bytes is the word in total
    protected int numberOfBytes;

    // Statically hold in memory how to represent this word as bytes. This way we only recalcuate when we want to
    protected String wordDataAsBinaryString = new String("");

    // Flag to tell if any data on the word has changed
    protected transient boolean fieldDataHasChanged = false;

    // Name of the word for identifying it later on
    protected String wordName = new String("");

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    protected transient List<PropertyChangeListener> dataChangeListeners = new ArrayList<>();

    // Whenever a field changes we should know bout it so we can update just the one and be more effecient
    protected transient Set<InterfaceDataField> changedFields = new HashSet<>();

    @Override
    public InterfaceDataWord clone(){
        InterfaceDataWord clone;
        try {
            clone = (InterfaceDataWord)Class.forName(this.getMyClassName()).getConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException | NoSuchMethodException | IllegalAccessException | ClassNotFoundException e) {
            // TODO throw to error handling service
            e.printStackTrace();
            return null;
        }

        clone.setWordName(this.getWordName());
        clone.numberOfBytes = this.numberOfBytes;
        clone.wordDataAsBinaryString = this.wordDataAsBinaryString;
        this.dataFields.stream().forEach( field -> {
            clone.addDataField(field.clone());
        });

        return clone;
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof InterfaceDataWord == false)
            return false;
        InterfaceDataWord otherWord = (InterfaceDataWord) other;
        if( !this.wordName.equals(otherWord.wordName) ||
                !this.wordDataAsBinaryString.equals(otherWord.wordDataAsBinaryString))
            return false;

        if( dataFields.size() != otherWord.dataFields.size() )
            return false;

        for( int fieldCount = 0; fieldCount < dataFields.size(); fieldCount++ ){
            if( !dataFields.get(fieldCount).equals(otherWord.dataFields.get(fieldCount)) )
                return false;
        }

        return true;
    }

    @Setter(AccessLevel.NONE)
    protected PropertyChangeListener myFieldListener = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            if(propertyChangeEvent.getSource() instanceof InterfaceDataField){
                fieldDataHasChanged = true;
                changedFields.add((InterfaceDataField)propertyChangeEvent.getSource());
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

}
