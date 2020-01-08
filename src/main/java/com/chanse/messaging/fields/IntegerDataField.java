package com.chanse.messaging.fields;

import com.chanse.messaging.utils.BitUtils;
import com.chanse.messaging.utils.MessagingSaveable;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.google.gson.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.lang.reflect.Type;
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
public class IntegerDataField extends InterfaceDataField<BigInteger> {

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

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }

}
