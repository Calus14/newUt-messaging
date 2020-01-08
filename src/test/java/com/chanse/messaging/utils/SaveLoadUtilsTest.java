package com.chanse.messaging.utils;

import com.chanse.messaging.Utils;
import com.chanse.messaging.fields.IntegerDataField;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.fields.StaticDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.messages.StandardMessage;
import com.chanse.messaging.msginterface.InterfaceDecoder;
import com.chanse.messaging.msginterface.StaticIdDecoder;
import com.chanse.messaging.words.InterfaceDataWord;
import com.chanse.messaging.words.StandardDataWord;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SaveLoadUtilsTest {

    /**
     * Test to verify that loading from known GSON File will create the correct Message, files named for what they should actually be
     */
    @Test
    public void saveLoadStandardMessagesFromFile(){
        String filePath = "src/test/resources/testSaveLoadStandardMessages.json";
        try {
            List<InterfaceMessage> messageList = new ArrayList<>();
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, false));
            for (int i = 0; i < 5; i++) {
                InterfaceMessage msg = Utils.getRandomStandardMessage(3);
                messageList.add(msg);
                writer.write(SaveLoadUtils.Instance.getMessageSaveString(msg));
                writer.newLine();
            }
            writer.close();

            List<InterfaceMessage> loadedMessages = SaveLoadUtils.Instance.loadMessagesFromFile(filePath);
            assertTrue(messageList.size() == loadedMessages.size());
            for(int i = 0; i < loadedMessages.size(); i++){
                assertTrue(loadedMessages.get(i).equals(messageList.get(i)));
            }
        }
        catch(Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected StaticIdDecoder staticIdDecoder1 = new StaticIdDecoder();
    protected StaticIdDecoder staticIdDecoder2 = new StaticIdDecoder();
    protected StaticIdDecoder staticIdDecoder3 = new StaticIdDecoder();

    /**
     * Test that will save and load a few InterfaceDecoders and verify that they are the same after retrieval as what was
     * created.
     */
    @Test
    public void saveLoadStaticIdDecoderFromFile() {
        String filePath1 = "src/test/resources/testSaveLoadStaticIdDecoder1.json";
        String filePath2 = "src/test/resources/testSaveLoadStaticIdDecoder2.json";
        String filePath3 = "src/test/resources/testSaveLoadStaticIdDecoder3.json";
        try{
            createStaticIdDecoders();
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath1, false));
            writer.write(SaveLoadUtils.Instance.getInterfaceDecoderSaveString(staticIdDecoder1));
            writer.newLine();
            writer.close();

            writer = new BufferedWriter(new FileWriter(filePath2, false));
            writer.write(SaveLoadUtils.Instance.getInterfaceDecoderSaveString(staticIdDecoder2));
            writer.newLine();
            writer.close();

            writer = new BufferedWriter(new FileWriter(filePath3, false));
            writer.write(SaveLoadUtils.Instance.getInterfaceDecoderSaveString(staticIdDecoder3));
            writer.newLine();
            writer.close();

            InterfaceDecoder decoder1 = SaveLoadUtils.Instance.loadInterfaceDecodersFromFile(filePath1).get(0);
            InterfaceDecoder decoder2 = SaveLoadUtils.Instance.loadInterfaceDecodersFromFile(filePath2).get(0);
            InterfaceDecoder decoder3 = SaveLoadUtils.Instance.loadInterfaceDecodersFromFile(filePath3).get(0);

            assertTrue( decoder1.equals(staticIdDecoder1) );
            assertTrue( decoder2.equals(staticIdDecoder2) );
            assertTrue( decoder3.equals(staticIdDecoder3) );
        }
        catch(Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    protected void createStaticIdDecoders() throws Exception{
        // will have 3 messages with 2 id's
        staticIdDecoder1.addIdPeekInfo(0, 5);
        staticIdDecoder1.addIdPeekInfo(5, 3);

        // will have 2 messages with just 1 id
        staticIdDecoder2.addIdPeekInfo(5, 3);

        // will have 0 messages with 4 id's
        staticIdDecoder3.addIdPeekInfo(0, 5);
        staticIdDecoder3.addIdPeekInfo(5, 3);
        staticIdDecoder3.addIdPeekInfo(8, 3);
        staticIdDecoder3.addIdPeekInfo(11, 3);

        // Message1
        StandardMessage message1 = new StandardMessage();
        StandardDataWord dataWord1 = new StandardDataWord();
        dataWord1.setNumberOfBytes(1);
        InterfaceDataField id1 = new StaticDataField();
        id1.setBitLength(5);
        id1.setBitOffset(0);
        id1.setName("Id 1");

        InterfaceDataField id2 = new StaticDataField();
        id2.setBitLength(3);
        id2.setBitOffset(5);
        id2.setName("Id 2");

        dataWord1.addDataField(id1);
        dataWord1.addDataField(id2);
        id1.setDataValue(new BigInteger("1"));
        id2.setDataValue(new BigInteger("1"));
        dataWord1.updateChangedFields();

        StandardDataWord dataWord2 = new StandardDataWord();
        dataWord2.setNumberOfBytes(2);

        InterfaceDataField field1 = new IntegerDataField();
        field1.setBitLength(7);
        field1.setBitOffset(0);
        field1.setName("Field1");

        InterfaceDataField field2 = new IntegerDataField();
        field2.setBitLength(1);
        field2.setBitOffset(8);
        field2.setName("Field2");

        InterfaceDataField field3 = new IntegerDataField();
        field3.setBitLength(7);
        field3.setBitOffset(9);
        field3.setName("Field3");

        dataWord2.addDataField(field1);
        dataWord2.addDataField(field2);
        dataWord2.addDataField(field3);
        field1.setDataValue(new BigInteger("1"));
        field2.setDataValue(new BigInteger("1"));
        field3.setDataValue(new BigInteger("1"));
        dataWord2.updateChangedFields();

        message1.addDataWord(dataWord1);
        message1.addDataWord(dataWord2);
        message1.setMessageName("Message1");
        message1.initializeMessageBinaryString();

        // Message2
        StandardMessage message2 = new StandardMessage();
        dataWord1 = new StandardDataWord();
        dataWord1.setNumberOfBytes(1);
        id1 = new StaticDataField();
        id1.setBitLength(5);
        id1.setBitOffset(0);
        id1.setName("Id 1_2");

        id2 = new StaticDataField();
        id2.setBitLength(3);
        id2.setBitOffset(5);
        id2.setName("Id 2_2");

        dataWord1.addDataField(id1);
        dataWord1.addDataField(id2);
        id1.setDataValue(new BigInteger("2"));
        id2.setDataValue(new BigInteger("2"));
        dataWord1.updateChangedFields();

        dataWord2 = new StandardDataWord();
        dataWord2.setNumberOfBytes(2);

        field1 = new StaticDataField();
        field1.setBitLength(7);
        field1.setBitOffset(0);
        field1.setName("Field1_2");

        field2 = new StaticDataField();
        field2.setBitLength(1);
        field2.setBitOffset(8);
        field2.setName("Field2_2");

        field3 = new StaticDataField();
        field3.setBitLength(7);
        field3.setBitOffset(9);
        field3.setName("Field3_2");

        dataWord2.addDataField(field1);
        dataWord2.addDataField(field2);
        dataWord2.addDataField(field3);
        field1.setDataValue(new BigInteger("2"));
        field2.setDataValue(new BigInteger("0"));
        field3.setDataValue(new BigInteger("2"));
        dataWord2.updateChangedFields();

        message2.addDataWord(dataWord1);
        message2.addDataWord(dataWord2);
        message2.setMessageName("Message2");
        message2.initializeMessageBinaryString();

        // Message3
        StandardMessage message3 = new StandardMessage();
        dataWord1 = new StandardDataWord();
        dataWord1.setNumberOfBytes(1);
        id1 = new StaticDataField();
        id1.setBitLength(5);
        id1.setBitOffset(0);
        id1.setName("Id 1_3");

        id2 = new StaticDataField();
        id2.setBitLength(3);
        id2.setBitOffset(5);
        id2.setName("Id 2_3");

        dataWord1.addDataField(id1);
        dataWord1.addDataField(id2);
        id1.setDataValue(new BigInteger("3"));
        id2.setDataValue(new BigInteger("3"));
        dataWord1.updateChangedFields();

        dataWord2 = new StandardDataWord();
        dataWord2.setNumberOfBytes(2);

        field1 = new StaticDataField();
        field1.setBitLength(7);
        field1.setBitOffset(0);
        field1.setName("Field1_3");

        field2 = new StaticDataField();
        field2.setBitLength(1);
        field2.setBitOffset(8);
        field2.setName("Field2_3");

        field3 = new StaticDataField();
        field3.setBitLength(7);
        field3.setBitOffset(9);
        field3.setName("Field3_3");

        dataWord2.addDataField(field1);
        dataWord2.addDataField(field2);
        dataWord2.addDataField(field3);
        field1.setDataValue(new BigInteger("3"));
        field2.setDataValue(new BigInteger("0"));
        field3.setDataValue(new BigInteger("3"));
        dataWord2.updateChangedFields();

        message3.addDataWord(dataWord1);
        message3.addDataWord(dataWord2);
        message3.setMessageName("Message3");
        message3.initializeMessageBinaryString();

        List<BigInteger> messageOneList = new ArrayList();
        List<BigInteger> messageTwoList = new ArrayList();
        List<BigInteger> messageThreeList = new ArrayList();

        messageOneList.add(new BigInteger("1"));
        messageOneList.add(new BigInteger("1"));

        messageTwoList.add(new BigInteger("2"));
        messageTwoList.add(new BigInteger("2"));

        messageThreeList.add(new BigInteger("3"));
        messageThreeList.add(new BigInteger("3"));

        staticIdDecoder1.addMessage(messageOneList, message1);
        staticIdDecoder1.addMessage(messageTwoList, message2);
        staticIdDecoder1.addMessage(messageThreeList, message3);

        messageOneList = new ArrayList();
        messageTwoList = new ArrayList();

        messageOneList.add(new BigInteger("1"));

        messageTwoList.add(new BigInteger("2"));

        staticIdDecoder2.addMessage(messageOneList, message1);
        staticIdDecoder2.addMessage(messageTwoList, message2);
    }
}
