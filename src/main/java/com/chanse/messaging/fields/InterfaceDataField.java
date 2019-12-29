package com.chanse.messaging.fields;

import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * InterfaceDataField is the base class for any field that will hold data for a given interface
 * These will generally just be basic types, such as enums string longs, ints, boolean etc.
 *
 * However it has been seen before that there can be more complex fields such as repeating fields.
 * static (non-changing fields) and even logistical or other fields that in general suck
 *
 * Finally, in order to give larger improvements these fields will all have to create and publish their
 * own data change listener so that we can gaurentee speed of application via only updating data when needing
 * too
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class InterfaceDataField {

    @Setter(AccessLevel.NONE)
    protected String myClassName = this.getClass().getName();

    // Name of the field itself. Must be unique to each message
    protected String name = new String("");
    // Number of bits this field consumes. NOTE this may not exist continguously in memory for things like combined fields
    protected long bitLength;
    // Offset, how many bits into its holding container it is starting
    protected long bitOffset;

    // The value the field in whatever object its binary represents
    protected Object dataValue;

    // The value of the field in binary but stored as a string
    protected transient String dataBinaryString = "";

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    // The Bean that the field holds to determine if a data change event needs to be fired off to other parts of the application
    protected transient List<PropertyChangeListener> myDataChangeListeners = new ArrayList<>();

    public void setBitLength(long bitLength){
        this.bitLength = bitLength;
        StringBuilder emptyBinaryStringBuilder = new StringBuilder();
        for(int i = 0; i<bitLength; i++)
            emptyBinaryStringBuilder.append("0");
        this.dataBinaryString = emptyBinaryStringBuilder.toString();
        this.updateDataValue();
    }

    // Flag to decide if it needs to recalculate the message to serial string
    protected transient boolean dataHasChanged;

    /**
     * Method to update the fields binaryString representation off of its data's object representation
     */
    protected abstract void updateBinaryString();

    /**
     * Method to update the data's object representation based off its binary string representation
     */
    protected abstract void updateDataValue();

    public void addFieldListener(PropertyChangeListener dataChangeListener){
        myDataChangeListeners.add(dataChangeListener);
    }
    public void removeFieldListener(PropertyChangeListener dataChangeListener){
        myDataChangeListeners.remove(dataChangeListener);
    }

    public void setDataValue(Object dataObject) throws Exception{
        if( !dataObject.getClass().equals(dataValue.getClass()) )
            throw new Exception("Tried to set data with class "+dataObject.getClass()+" on field named "+name+
                                 " which holds data with of type "+dataValue.getClass());

        if( dataObject.equals(this.dataValue) )
            return;
        Object oldData = this.dataValue;
        this.dataValue = dataObject;
        this.updateBinaryString();
        dataHasChanged = true;
        myDataChangeListeners.stream().forEach(listener -> {
            listener.propertyChange(new PropertyChangeEvent(this, "dataValue", oldData, dataObject));
        });
    }

    public void setDataBinaryString(String dataBinaryString){
        if( dataBinaryString.equals(this.dataBinaryString) )
            return;

        String oldBinaryString = this.dataBinaryString;
        this.dataBinaryString = dataBinaryString;
        this.updateDataValue();
        dataHasChanged = true;
        myDataChangeListeners.stream().forEach(listener -> {
            listener.propertyChange(new PropertyChangeEvent(this, "dataBinaryString", oldBinaryString, dataBinaryString));
        });
    }
}
