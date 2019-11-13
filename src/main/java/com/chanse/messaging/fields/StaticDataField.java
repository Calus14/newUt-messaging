package com.chanse.messaging.fields;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;

/**
 * The static data field exists for fields that will always the same value and same position
 * Specifically they should never be able to throw to a listener for their data changing and not ever call into
 * a front end service that shows its data has changed
 */
@Data
@AllArgsConstructor
public class StaticDataField extends InterfaceDataField {

    @Override
    public void setDataHasChanged(boolean dataHasChanged) {
        super.setDataHasChanged(false);
    }

    @Override
    public void setDataValue(BigInteger dataValue){
        // Return;
    }

    @Override
    public void createAndAttachMyDataChangeListener() {
        myDataChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                // Do nothing. That is what makes the static Data Field Unique
            }
        };
    }

    @Override
    public String getFieldAsBinaryString() {
        return this.dataValue.toString(2);
    }
}
