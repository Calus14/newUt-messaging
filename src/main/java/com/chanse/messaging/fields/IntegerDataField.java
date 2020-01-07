package com.chanse.messaging.fields;

import com.chanse.messaging.utils.BitUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigInteger;

/**
 * Standard class for most all fields on a message interface. Simply holds an integer value across some bits
 * On change of any of these values its local data will be updated and its flag will also be updated but its on the word
 * or message or whoever to actually go update the entire data
 *
 * Data value of a Integer Data Field will be a BigInteger
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IntegerDataField extends InterfaceDataField {

    @Override
    protected void updateBinaryString() {
        if(dataValue == null){
            this.dataValue = new BigInteger("0");
            this.dataBinaryString = "";
        }
        else {
            this.dataBinaryString = BitUtils.getBinaryStringFromBigInt((BigInteger) dataValue, (int) bitLength);
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
}
