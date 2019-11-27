package com.chanse.messaging.fields;

import com.chanse.messaging.bitUtils.StandardUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.math.BigInteger;

/**
 * The static data field exists for fields that will always the same value and same position
 * Specifically they should never be able to throw to a listener for their data changing and not ever call into
 * a front end service that shows its data has changed
 *
 * The Data Value that a Static Data Field will hold will be a BigInteger
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class StaticDataField extends InterfaceDataField {

    @Override
    public void setDataHasChanged(boolean dataHasChanged) {
        super.setDataHasChanged(false);
    }

    @Override
    protected void updateBinaryString() {
        if(dataValue == null){
            this.dataValue = new BigInteger("0");
            this.dataBinaryString = "";
        }
        else {
            this.dataBinaryString = StandardUtils.getBinaryStringFromBigInt((BigInteger) dataValue, (int) bitLength);
        }
    }

    @Override
    protected void updateDataValue() {
        if(this.dataBinaryString == null){
            this.dataBinaryString = "";
            this.dataValue = new BigInteger("0");
        }
        else{
            this.dataValue = new BigInteger(this.dataBinaryString, 2);
        }
    }

    @Override
    public void setDataValue(Object dataValue){
        // Return;
    }

    @Override
    public void setDataBinaryString(String dataBinaryString){
        // Return;
    }

}
