package com.chanse.messaging.msginterface;

import com.chanse.messaging.Utils;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.messages.StandardMessage;
import com.chanse.messaging.words.InterfaceDataWord;
import com.chanse.messaging.words.StandardDataWord;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static com.chanse.messaging.Utils.getRandomIntegerDataField;
import static org.junit.Assert.*;

/**
 * Test class for the static ID Decoder to verify that we can decode messages from an input stream correctly
 */
public class StaticIdDecoderTest {

    /**
     * Test that validates after adding a number of IDPeakInfo that non overlap
     */
    @Test
    public void validateIdPeekInfo() {
        try {
            StaticIdDecoder testDecoder = new StaticIdDecoder(null);

            testDecoder.addIdPeekInfo(0, 5);
            testDecoder.addIdPeekInfo(8, 5);
            testDecoder.addIdPeekInfo(13, 3);
            testDecoder.addIdPeekInfo(16, 1);
            testDecoder.validateIdPeekInfo();
        }
        catch(Exception e){
            fail("Failed because an exception arose while adding peak info that is valid");
        }

        try {
            StaticIdDecoder testDecoder = new StaticIdDecoder(null);

            testDecoder.addIdPeekInfo(0, 5);
            testDecoder.addIdPeekInfo(8, 5);
            testDecoder.addIdPeekInfo(13, 3);
            testDecoder.addIdPeekInfo(16, 10);
            testDecoder.addIdPeekInfo(24, 10);
            testDecoder.validateIdPeekInfo();
            fail("Added colliding peek info but no exception was thrown");
        }
        catch(Exception e){
            // It is supposed to fail so this is good.
        }
    }

    @Test
    public void addMessage() {

        StaticIdDecoder testDecoder = new StaticIdDecoder(null);
        try {
            testDecoder = new StaticIdDecoder(null);
            testDecoder.addIdPeekInfo(0, 5);
            testDecoder.addIdPeekInfo(8, 5);
        }
        catch(Exception e){
            fail("Failed because an exception arose while adding peak info that is valid");
        }

        // Validate that can actually add in multiple messages with different IDS
        try{
            List<BigInteger> id1 = new ArrayList();
            id1.add(new BigInteger("1"));
            id1.add(new BigInteger("3"));
            testDecoder.addMessage(id1, new StandardMessage());

            List<BigInteger> id2 = new ArrayList();
            id2.add(new BigInteger("1"));
            id2.add(new BigInteger("1"));
            testDecoder.addMessage(id2, new StandardMessage());

            List<BigInteger> id3 = new ArrayList();
            id3.add(new BigInteger("2"));
            id3.add(new BigInteger("3"));
            testDecoder.addMessage(id3, new StandardMessage());

            List<BigInteger> id4 = new ArrayList();
            id4.add(new BigInteger("5"));
            id4.add(new BigInteger("3"));
            testDecoder.addMessage(id4, new StandardMessage());
        }
        catch(Exception e){
            fail("Failed to add in 4 seperate messages to a staticIdDecoder");
        }

        // Validate that we have an exception when adding in messages that have the same overlap
        try{
            List<BigInteger> id1 = new ArrayList();
            id1.add(new BigInteger("1"));
            id1.add(new BigInteger("3"));
            testDecoder.addMessage(id1, new StandardMessage());

            List<BigInteger> id2 = new ArrayList();
            id2.add(new BigInteger("1"));
            id2.add(new BigInteger("3"));
            testDecoder.addMessage(id2, new StandardMessage());
            fail("Added colliding id info to the static ID but no exception was thrown");
        }
        catch(Exception e){
            // We Excpected this exception to be thrown
        }

        // Validate that we have an exception when adding in messages that not enough ID's
        try{
            List<BigInteger> id1 = new ArrayList();
            id1.add(new BigInteger("1"));
            id1.add(new BigInteger("3"));
            id1.add(new BigInteger("3"));
            testDecoder.addMessage(id1, new StandardMessage());

            List<BigInteger> id2 = new ArrayList();
            id2.add(new BigInteger("1"));
            id2.add(new BigInteger("0"));
            testDecoder.addMessage(id2, new StandardMessage());
            fail("Added colliding id info to the static ID but no exception was thrown despite not enough ID's being provided");
        }
        catch(Exception e){
            // We Excpected this exception to be thrown
        }
    }

    /**
     * Test will add two messages with unique names to the staticIdDecoder and then set the input stream on the decoder
     * then pull the messages out in an expected order
     */
    @Test
    public void decodeMessages() {
        try{
            StaticIdDecoder testDecoder = new StaticIdDecoder(null);

            testDecoder = new StaticIdDecoder(null);
            testDecoder.addIdPeekInfo(0, 5);
            testDecoder.addIdPeekInfo(8, 5);

            StandardMessage message1 = new StandardMessage();
            StandardDataWord dataWord = new StandardDataWord();
            dataWord.setNumberOfBytes(3);
            InterfaceDataField id1 = getRandomIntegerDataField(5, 0);
            InterfaceDataField id2 = getRandomIntegerDataField(5, 8);

            dataWord.addDataField(id1);
            dataWord.addDataField(id2);

            id1.setDataValue(new BigInteger("1"));
            id2.setDataValue(new BigInteger("2"));
            dataWord.updateChangedFields();

            message1.addDataWord(dataWord);
            message1.setMessageName("Message1");
            message1.initializeMessageBinaryString();

            StandardMessage message2 = new StandardMessage();
            StandardDataWord dataWord2 = new StandardDataWord();
            dataWord2.setNumberOfBytes(3);
            InterfaceDataField id3 = getRandomIntegerDataField(5, 0);
            InterfaceDataField id4 = getRandomIntegerDataField(5, 8);

            dataWord2.addDataField(id4);
            dataWord2.addDataField(id3);

            id3.setDataValue(new BigInteger("3"));
            id4.setDataValue(new BigInteger("4"));
            dataWord2.updateChangedFields();

            message2.addDataWord(dataWord2);
            message2.setMessageName("Message2");
            message2.initializeMessageBinaryString();

            List<BigInteger> idList1 = new ArrayList();
            idList1.add(new BigInteger("1"));
            idList1.add(new BigInteger("2"));
            testDecoder.addMessage(idList1, message1);

            List<BigInteger> idList2 = new ArrayList();
            idList2.add(new BigInteger("3"));
            idList2.add(new BigInteger("4"));
            testDecoder.addMessage(idList2, message2);

            File binaryDataFile = new File("src/test/resources/testWriteFile.bin");
            FileOutputStream outStream = new FileOutputStream(binaryDataFile, false);
            byte[] msg1AsBytes = new BigInteger( message1.getMessageAsSerialString().toString(), 2).toByteArray();
            byte[] msg2AsBytes = new BigInteger( message2.getMessageAsSerialString().toString(), 2).toByteArray();

            // write 2 of message 1, then 3 of message 2
            outStream.write(msg1AsBytes);
            outStream.write(msg1AsBytes);
            outStream.write(msg2AsBytes);
            outStream.write(msg2AsBytes);
            outStream.write(msg2AsBytes);
            outStream.close();

            binaryDataFile = new File("src/test/resources/testWriteFile.bin");
            FileInputStream inStream = new FileInputStream(binaryDataFile);
            testDecoder.setInputStream(inStream);
            List<InterfaceMessage> messagesWritten = testDecoder.decodeMessages();
            assertTrue(messagesWritten.size() == 5);
            assertTrue(messagesWritten.get(0).getMessageName().equals("Message1"));
            assertTrue(messagesWritten.get(1).getMessageName().equals("Message1"));
            assertTrue(messagesWritten.get(2).getMessageName().equals("Message2"));
            assertTrue(messagesWritten.get(3).getMessageName().equals("Message2"));
            assertTrue(messagesWritten.get(4).getMessageName().equals("Message2"));
        }
        catch(Exception e){
            // TODO
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}