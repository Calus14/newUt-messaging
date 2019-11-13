package com.chanse.messaging.fields;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;
import java.util.List;

/**
 * The Enum Data fields is a standard interface Data field that holds the model for its data changing. It reacts to the value
 * of the field changing only if the value of the enum itself has changed. Multiple interfaces will have multiple enums
 * mapping to the same value.
 */
@Data
@AllArgsConstructor
public class EnumDataField extends InterfaceDataField {

    /**
     * An internal class for the purpose of allowing us to use spring libraries in the future if we want to rather
     * than the native java enum This could be very helpfull for communicating intraservice, saving/loading, and suprisingly taxes!
     */
    @Data
    @AllArgsConstructor
    protected class FieldSpecificEnum{
        protected String enumName;
        protected Integer enumValue;
    }

    protected List<FieldSpecificEnum> allowedEnums;

    @Override
    public void createAndAttachMyDataChangeListener() {
        myDataChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                // Only check if the value on the line has changed.
                if( ((FieldSpecificEnum)propertyChangeEvent.getNewValue()).getEnumValue() != ((FieldSpecificEnum)propertyChangeEvent.getOldValue()).getEnumValue()){
                    dataHasChanged = true;
                    dataValue = new BigInteger( ((FieldSpecificEnum)propertyChangeEvent.getNewValue()).getEnumValue().toString());
                }
            }
        };
    }

    @Override
    public String getFieldAsBinaryString() {
        return this.dataValue.toString(2);
    }
}
