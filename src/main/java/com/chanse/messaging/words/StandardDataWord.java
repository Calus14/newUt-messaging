package com.chanse.messaging.words;


import com.chanse.messaging.utils.BitUtils;
import com.chanse.messaging.exceptions.BadFieldWriteException;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Standard DataWord Class that most interfaces will use with exception in a few cases.
 * Data words will generally have identifying fields that are just static fields which define how to unpack the rest of
 * the bytes
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class StandardDataWord extends InterfaceDataWord{

    @Override
    public void updateChangedFields() throws BadFieldWriteException{
        // this is bad??? I have no idea
        final StringBuilder exceptionBuilder = new StringBuilder();

        this.changedFields.stream().forEach( field -> {
            try {
                BitUtils.insertFieldIntoWord(this, field);
                field.setDataHasChanged( false );
            }
            catch( BadFieldWriteException bfwe){
                exceptionBuilder.append (bfwe.getMessage() );
            }
        } );
        if(exceptionBuilder.length() != 0) {
            throw new BadFieldWriteException(exceptionBuilder.toString());
        }
    }

    @Override
    public void setNumberOfBytes(int numberOfBytes) {
        this.numberOfBytes = numberOfBytes;
        StringBuilder blankBuilder = new StringBuilder();
        for(int i = 0; i < numberOfBytes*8; i++)
            blankBuilder.append('0');
        this.wordDataAsBinaryString = blankBuilder.toString();

        // Because we reset the internal storage, we need to update all fields that are part of the standard data word
        this.changedFields.clear();
        this.dataFields.stream().forEach(field -> {this.changedFields.add(field); });

        try{
            updateChangedFields();
        }
        catch( BadFieldWriteException bfwe ){
            for(int i =0; i<100; i++)
                System.out.println("Holy shit broken stuff enable logging for slf4j please you idiot.");
        }
    }
}
