package com.chanse.messaging.fields;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.PropertyChangeListener;
import java.math.BigInteger;

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
    protected String name;
    // Number of bits this field consumes. NOTE this may not exist continguously in memory for things like combined fields
    protected long bitLength;
    // Offset, how many bits into its holding container it is starting
    protected long bitOffset;

    // The value that the given field holds in BINARY. This may not be the value it holds that the user will see
    protected BigInteger dataValue;
    // The Bean that the field holds to determine if a data change event needs to be fired off to other parts of the application
    protected PropertyChangeListener myDataChangeListener;

    // Flag to decide if it needs to recalculate the message to serial string
    protected boolean dataHasChanged;

    // Method that should be called on all fields in a message before being "created"
    public abstract void createAndAttachMyDataChangeListener();

    // Method to give the correct binary representation of the field as a binary String
    public abstract String getFieldAsBinaryString();
}
