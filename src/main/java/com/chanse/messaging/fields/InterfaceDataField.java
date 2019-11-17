package com.chanse.messaging.fields;

import lombok.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
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

    // Name of the field itself. Must be unique to each message
    protected String name = new String("");
    // Number of bits this field consumes. NOTE this may not exist continguously in memory for things like combined fields
    protected long bitLength;
    // Offset, how many bits into its holding container it is starting
    protected long bitOffset;

    // The value that the given field holds in BINARY. This may not be the value it holds that the user will see
    protected BigInteger dataValue = new BigInteger("0");

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    // The Bean that the field holds to determine if a data change event needs to be fired off to other parts of the application
    protected List<PropertyChangeListener> myDataChangeListeners = new ArrayList<>();

    // Flag to decide if it needs to recalculate the message to serial string
    protected boolean dataHasChanged;

    // Method to give the correct binary representation of the field as a binary String
    public abstract String getFieldAsBinaryString();

    public void addFieldListener(PropertyChangeListener dataChangeListener){
        myDataChangeListeners.add(dataChangeListener);
    }
    public void removeFieldListener(PropertyChangeListener dataChangeListener){
        myDataChangeListeners.remove(dataChangeListener);
    }

    public void setDataValue(BigInteger data){
        if(data.toString() == this.dataValue.toString())
            return;
        BigInteger oldData = this.dataValue;
        this.dataValue = data;
        dataHasChanged = true;
        myDataChangeListeners.stream().forEach(listener -> {
            listener.propertyChange(new PropertyChangeEvent(this, "dataValue", oldData, data));
        });
    }
}
