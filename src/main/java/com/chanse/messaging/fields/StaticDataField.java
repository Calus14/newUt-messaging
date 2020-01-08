package com.chanse.messaging.fields;

import com.chanse.messaging.utils.BitUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * The static data field exists for fields that will always the same value and same position
 * Specifically they should never be able to throw to a listener for their data changing and not ever call into
 * a front end service that shows its data has changed
 *
 * The Data Value that a Static Data Field will hold will be a BigInteger
 */
@Data
public class StaticDataField extends InterfaceDataField<BigInteger> {

    public StaticDataField(){
        this.dataValue = null;
        this.dataBinaryString = null;
    }

    @Override
    public void setDataHasChanged(boolean dataHasChanged) {
        super.setDataHasChanged(false);
    }

    @Override
    public void setBitLength(long bitLength){
        this.bitLength = bitLength;
    }

    @Override
    protected void updateBinaryString() {
        if(dataValue != null){
            this.dataBinaryString = BitUtils.getBinaryStringFromBigInt((BigInteger) dataValue, (int) bitLength);
        }
    }

    @Override
    protected void updateDataValue() {
        if(this.dataBinaryString != null){
            this.dataValue = new BigInteger(this.dataBinaryString, 2);
        }
    }

    @Override
    public void setDataValue(BigInteger dataValue) throws Exception{
        if(this.dataValue == null) {
            super.setDataValue(dataValue);
        }
    }

    @Override
    public void setDataBinaryString(String dataBinaryString){
        if(this.dataBinaryString == null){
            super.setDataBinaryString(dataBinaryString);
        }
    }

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }
}
