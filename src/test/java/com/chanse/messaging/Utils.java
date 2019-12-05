package com.chanse.messaging;

import com.chanse.messaging.fields.IntegerDataField;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.messages.StandardMessage;
import com.chanse.messaging.words.InterfaceDataWord;
import com.chanse.messaging.words.StandardDataWord;

import java.math.BigInteger;
import java.util.Random;

/**
 * Static helper class to help with creating data that will be used to test
 */
public class Utils {

    public static Random myRandom = new Random();

    public static InterfaceDataField getRandomIntegerDataField(int bits, int offset){
        IntegerDataField randomField = new IntegerDataField();

        randomField.setBitLength(bits);
        randomField.setBitOffset(offset);

        int randomIntValue =  bits > 1 ? myRandom.nextInt( (int)Math.pow((double)bits-1, 2.0) ) : myRandom.nextInt() % 2;
        StringBuilder randomBuilder = new StringBuilder();
        for(int i = 0; i<bits; i++){
            if( myRandom.nextBoolean() )
                randomBuilder.append("1");
            else
                randomBuilder.append("0");
        }
        try {
            randomField.setDataBinaryString(randomBuilder.toString());
        }
        catch(Exception e){
            System.out.println(e.getMessage());
        }

        return randomField;
    }

    public static InterfaceDataWord getRandomStandardDataWord(int bytes){
        StandardDataWord randomWord = new StandardDataWord();

        randomWord.setNumberOfBytes(bytes);
        // TODO generate random fields by picking a random value from 1-32 for a field and then adding it and doing this repeatedly until we have
        // all the bytes filled out. Also put a 1/6 chance that there are no fields in this word. (spare fields).
        int bitsLeft = bytes*8;
        int bitOffset = 0;
        while(bitsLeft > 0){
            // 1/6 chance that there is no field next (spare fields)
            boolean isSpareField = myRandom.nextInt()%6==0;
            int fieldBits = Math.abs((myRandom.nextInt() % 32))+1;
            fieldBits = fieldBits > bitsLeft ? bitsLeft : fieldBits;

            // Build the field and set a name on it.
            if( !isSpareField) {
                InterfaceDataField randomField = getRandomIntegerDataField(fieldBits, bitOffset);
                randomField.setName("RandomField" + randomWord.getDataFields().size());
                randomWord.addDataField(randomField);
                randomWord.getChangedFields().add(randomField);
            }

            bitOffset += fieldBits;
            bitsLeft -= fieldBits;
        }

        try {
            randomWord.updateChangedFields();
        }
        catch(Exception e){
            System.out.println("Failed to create a random word with exception "+e.getMessage());
        }
        return randomWord;
    }

    static int messageCount = 0;
    public static InterfaceMessage getRandomStandardMessage(int wordCount){
        StandardMessage randomMessage = new StandardMessage();

        for( int i = 0; i<wordCount; i++){
            // Random Number of Bytes for this word
            int wordLength = Math.abs(myRandom.nextInt()%20);
            randomMessage.addDataWord(getRandomStandardDataWord(wordLength));
        }

        randomMessage.setMessageName("RandomMessage"+messageCount);
        randomMessage.initializeMessageBinaryString();
        return randomMessage;
    }
}
