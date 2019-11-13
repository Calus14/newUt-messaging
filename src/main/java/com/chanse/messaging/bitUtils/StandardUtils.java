package com.chanse.messaging.bitUtils;

import com.chanse.messaging.exceptions.BadFieldWriteException;
import com.chanse.messaging.exceptions.BaseMessagingException;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.words.InterfaceDataWord;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Standard Utils in the messaging service will be doing operations like writing a field into a given bit arrary,
 * validating changing complementness of a field, endianness, double point precission and larger than 64 bit handling
 */
public class StandardUtils {

    // Convience because i can
    final int oneBits = 0x1;
    final int twoBits = 0x3;
    final int threeBit = 0x7;
    final int fourBits = 0x15;
    final int fiveBits = 0x31;
    final int sixBits = 0x63;
    final int sevenBits = 0x127;
    final int eightBits = 0x255;

    /**
     * Method to peak at a inputStreams data and return the value of a set of bits anywhere on the stream
     * @param offset what bit to start looking at
     * @param length how many bits to read in
     * @param inputData the data of the input stream
     * @return a Big Integer representing the value of the data in that section of the stream
     * @throws BadFieldWriteException If the data requested is not possible to be viewed at
     */
    // TODO this may be improvable by looking at only specific bytes andnot using big integer and string manipulation
    public static BigInteger peekAtBits(int offset, int length, byte[] inputData) throws BadFieldWriteException{
        if(offset+length > inputData.length * 8)
            throw new BadFieldWriteException("Unable to peek at bits on the given inputData stream because the offset:"+offset+
                    " and length:" +length+ " overflows the input data with "+inputData.length+" bytes.");

        String inputAsBinaryString = new BigInteger(inputData).toString(2);
        return new BigInteger(inputAsBinaryString.substring(offset, length), 2);
    }

    /**
     * Util method that takes a template message and attempts to fill it abstractly from the given binary data
     *
     * @param message Message to be filled in
     * @param inputData binary data to fill the message from. Data will be reduced and read data will be removed.
     * @return the number of bytes read to fill the message.
     * @throws BaseMessagingException if any error occured trying to read a word or field
     */
    public static int fillMessageFromData(InterfaceMessage message, byte[] inputData) throws BaseMessagingException {
        StringBuffer errorMessage = new StringBuffer();
        int totalBytesRead = 0;

        for(InterfaceDataWord word : message.getDataWords()){
            try{
                int bytesRead = fillWordFromData(word, inputData);
                inputData = Arrays.copyOfRange(inputData, bytesRead, inputData.length);
                totalBytesRead += bytesRead;
            }catch(Exception e){
                errorMessage.append(e.getMessage()+"\n");
            }
        }
        if(!errorMessage.toString().isEmpty())
            throw new BaseMessagingException("Failed to fill message with name: "+message.getMessageName()+". Here are the following errors:\n"+
                    errorMessage.toString());
        return totalBytesRead;
    }

    /**
     * Util method that takes a word and attempts to fill it abstractly from the given binary data
     * @param word Word to be filled in
     * @param inputData binary data to fill the message from. Data will be reduced and read data will be removed.
     * @return the number of bytes read to fill the message.
     * @throws BaseMessagingException if any error occured trying to read a word or field
     */
    public static int fillWordFromData(InterfaceDataWord word, byte[] inputData) throws BaseMessagingException {
        //Allowing us to use lambdas
        AtomicInteger totalBitsRead = new AtomicInteger();
        StringBuffer errorMessage = new StringBuffer();

        // We are avoiding using the peek method so that we dont have to constantly create a new binary string
        String binaryString = new BigInteger(inputData).toString(2);

        word.getDataFields().stream().forEach( field -> {

            if(field.getBitOffset() + field.getBitLength() > binaryString.length())
                errorMessage.append("Unable to read field: "+field.getName()+" because the given field over extends how much input data there was.\n");

            field.setDataValue( new BigInteger(binaryString.substring((int)field.getBitOffset(), (int)field.getBitLength())) );

            // We want to know whats the farthest big we read. Doing this means we can ignore spare bits or fields that
            // have yet to be marked.
            if(field.getBitLength()+field.getBitOffset() > totalBitsRead.get())
                totalBitsRead.set((int)(field.getBitLength()+field.getBitOffset()));
        });
        if(!errorMessage.toString().isEmpty())
            throw new BaseMessagingException("Failed to read message word with name: "+word.getWordName()+" Here are the errors:\n"
                    +errorMessage.toString());

        // There may be spare bits we havent read yet at the end. but words are on the byte level so round up to the nearest byte
        int totalBytesRead = totalBitsRead.get() % 8 == 0? totalBitsRead.get() / 8 : totalBitsRead.get() / 8 + 1;
        return totalBytesRead;
    }

    /**
     * Util method that changes only the data for a given field on a word. This is usefull for cases where we only want to change
     * one field of a word. Allowing us to not have to re-write every word for every send.
     * @param word The word that wishes to have its model changed
     * @param field the Field that will have its data inserted into the word
     * @throws BadFieldWriteException
     */
    public static void insertFieldIntoWord(InterfaceDataWord word, InterfaceDataField field) throws BadFieldWriteException {
        // Check to see if the word not enough room to even write this field int
        if ((word.getWordData().toByteArray().length * 8) < field.getBitOffset() + field.getBitLength())
            throw new BadFieldWriteException("Word with name " + word.getWordName() + " has only a data buffer of " + word.getWordData().toByteArray().length
                    + "\nWe are trying to place a field named " + field.getName() + " into it who has an offset of " + field.getBitOffset() + " and length of "
                    + field.getBitLength());

        //TODO This method COULD be improved if done using purely primitives and binary operators. Using a String
        // Object and a big Integer Object has a cost associated with it.

        String fieldDataBinaryString = field.getFieldAsBinaryString();
        StringBuffer wordDataBinaryString = new StringBuffer(word.getWordData().toString(2));
        wordDataBinaryString.replace((int) field.getBitOffset(), (int) (field.getBitOffset() + field.getBitLength()), fieldDataBinaryString);
        word.setWordData(new BigInteger(wordDataBinaryString.toString(), 2));
    }
}
