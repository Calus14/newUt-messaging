package com.chanse.messaging.fields;

import com.chanse.messaging.utils.BitUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;
import java.util.List;

/**
 * The Enum Data fields is a standard interface Data field that holds the model for its data changing. It reacts to the value
 * of the field changing only if the value of the enum itself has changed. Multiple interfaces will have multiple enums
 * mapping to the same value.
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class EnumDataField extends InterfaceDataField {

    static final FieldSpecificEnum UT_ERROR_ENUM = new FieldSpecificEnum("UT_Internal_Error", 0);

    /**
     * An internal class for the purpose of allowing us to use spring libraries in the future if we want to rather
     * than the native java enum This could be very helpfull for communicating intraservice, saving/loading, and suprisingly taxes!
     */
    @Data
    @AllArgsConstructor
    static protected class FieldSpecificEnum{
        protected String enumName;
        protected Integer enumValue;
    }

    protected List<FieldSpecificEnum> allowedEnums;

    @Override
    protected void updateBinaryString() {
        BigInteger bigIntEnumRepresentation = BigInteger.valueOf( ((FieldSpecificEnum)dataValue).enumValue.intValue() );
        this.dataBinaryString = BitUtils.getBinaryStringFromBigInt(bigIntEnumRepresentation, (int) bitLength);
    }

    @Override
    protected void updateDataValue() {
        if(this.dataBinaryString == null){
            this.dataBinaryString = "";
            this.dataValue = UT_ERROR_ENUM;
        }
        else{
            int intFieldValue = new BigInteger(this.dataBinaryString, 2).intValue();
            for(FieldSpecificEnum e : allowedEnums){
                if(e.enumValue.intValue() == intFieldValue){
                    this.dataValue = e;
                    break;
                }
            }
        }
    }
}
