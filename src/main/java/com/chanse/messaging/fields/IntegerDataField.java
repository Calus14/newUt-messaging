package com.chanse.messaging.fields;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;

/**
 * Standard class for most all fields on a message interface. Simply holds an integer value across some bits
 * On change of any of these values its local data will be updated and its flag will also be updated but its on the word
 * or message or whoever to actually go update the entire data
 */
@Data
@AllArgsConstructor
public class IntegerDataField extends InterfaceDataField {


    @Override
    public void createAndAttachMyDataChangeListener() {
        myDataChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                if( ((Integer)propertyChangeEvent.getNewValue()).intValue() !=
                    ((Integer)propertyChangeEvent.getOldValue()).intValue() )
                {
                    dataHasChanged = true;
                    dataValue = (BigInteger)propertyChangeEvent.getNewValue();
                }
            }
        };
    }

    @Override
    // TODO handle negative value and sign extention
    public String getFieldAsBinaryString() {
        return this.dataValue.toString(2);
    }
}
