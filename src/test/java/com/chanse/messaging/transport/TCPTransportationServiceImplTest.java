package com.chanse.messaging.transport;

import com.chanse.messaging.fields.IntegerDataField;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.fields.StaticDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.messages.StandardMessage;
import com.chanse.messaging.msginterface.StaticIdDecoder;
import com.chanse.messaging.utils.SaveLoadUtils;
import com.chanse.messaging.words.StandardDataWord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


class TCPTransportationServiceImplTest {

    TCPClientTransportationService myClientServiceImpl = new TCPClientTransportationService();
    TCPServerTransportationService myServerServiceImpl = new TCPServerTransportationService();

    StaticIdDecoder clientDecoder = new StaticIdDecoder();
    StaticIdDecoder serverDecoder = new StaticIdDecoder();

    StandardMessage message1;
    StandardMessage message2;
    StandardMessage message3;

    @BeforeEach
    protected void setup() throws Exception{
        setUpDecoders();

        // Bind the client on port 6001
        // Bind the server on port 6002
        myClientServiceImpl.setReceivePort(9003);
        myClientServiceImpl.setSendPort(9005);
        myClientServiceImpl.setSendHostAddress("127.0.0.1");
        myClientServiceImpl.setDecoder(clientDecoder);
        myClientServiceImpl.setup();

        myServerServiceImpl.setReceivePort(9005);
        myServerServiceImpl.setWaitTimeoutSeconds(30);
        myServerServiceImpl.setDecoder(serverDecoder);
        myServerServiceImpl.setup();
    }

    @AfterEach
    public void teardown(){
        try {
            myClientServiceImpl.disconnect();
            myServerServiceImpl.disconnect();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void sendAndReceiveMessage() {
        try {
            myClientServiceImpl.connect();
            myServerServiceImpl.connect();
        }
        catch(Exception e){
            e.printStackTrace();
            fail(e.getMessage()+" Was the error while trying to setup the test service");
        }

        byte[] msg1AsBytes = new BigInteger( message1.getMessageAsSerialString().toString(), 2).toByteArray();
        byte[] msg2AsBytes = new BigInteger( message2.getMessageAsSerialString().toString(), 2).toByteArray();
        byte[] msg3AsBytes = new BigInteger( message3.getMessageAsSerialString().toString(), 2).toByteArray();

        //CLIENT TO SERVER
        myClientServiceImpl.sendMessage(msg1AsBytes);
        myClientServiceImpl.sendMessage(msg2AsBytes);
        myClientServiceImpl.sendMessage(msg3AsBytes);

        List<InterfaceMessage> serversMessages = myServerServiceImpl.receiveMessages();
        assertTrue(serversMessages.size() == 3);

        assertTrue( serversMessages.get(0).equals(message1));
        assertTrue( serversMessages.get(1).equals(message2));
        assertTrue( serversMessages.get(2).equals(message3));

        myClientServiceImpl.sendMessage(msg1AsBytes);
        myClientServiceImpl.sendMessage(msg1AsBytes);
        myClientServiceImpl.sendMessage(msg1AsBytes);
        myClientServiceImpl.sendMessage(msg3AsBytes);
        myClientServiceImpl.sendMessage(msg2AsBytes);
        myClientServiceImpl.sendMessage(msg1AsBytes);

        serversMessages = myServerServiceImpl.receiveMessages();
        assertTrue( serversMessages.get(0).equals(message1));
        assertTrue( serversMessages.get(1).equals(message1));
        assertTrue( serversMessages.get(2).equals(message1));
        assertTrue( serversMessages.get(3).equals(message3));
        assertTrue( serversMessages.get(4).equals(message2));
        assertTrue( serversMessages.get(5).equals(message1));

        //SERVER TO CLIENT
        myServerServiceImpl.sendMessage(msg1AsBytes);
        myServerServiceImpl.sendMessage(msg2AsBytes);
        myServerServiceImpl.sendMessage(msg3AsBytes);

        serversMessages = myClientServiceImpl.receiveMessages();
        assertTrue(serversMessages.size() == 3);

        assertTrue( serversMessages.get(0).equals(message1));
        assertTrue( serversMessages.get(1).equals(message2));
        assertTrue( serversMessages.get(2).equals(message3));

        myServerServiceImpl.sendMessage(msg1AsBytes);
        myServerServiceImpl.sendMessage(msg1AsBytes);
        myServerServiceImpl.sendMessage(msg1AsBytes);
        myServerServiceImpl.sendMessage(msg3AsBytes);
        myServerServiceImpl.sendMessage(msg2AsBytes);
        myServerServiceImpl.sendMessage(msg1AsBytes);

        serversMessages = myClientServiceImpl.receiveMessages();
        assertTrue( serversMessages.get(0).equals(message1));
        assertTrue( serversMessages.get(1).equals(message1));
        assertTrue( serversMessages.get(2).equals(message1));
        assertTrue( serversMessages.get(3).equals(message3));
        assertTrue( serversMessages.get(4).equals(message2));
        assertTrue( serversMessages.get(5).equals(message1));
    }

    @Test
    public void connect() {
        try {
            myClientServiceImpl.connect();
            myServerServiceImpl.connect();
        }
        catch(Exception e){
            e.printStackTrace();
            fail(e.getMessage()+" Was the error while trying to setup the test service");
        }
    }

    /**
     * Test designed to make sure that we can save a transport service to json and l oad it from json. If this is broken
     * then dynamic configuration of the comsim services transport layer will be broken as well.
     */
    @Test
    public void loadFromJson(){
        String clientAsJson = SaveLoadUtils.Instance.getTransportServiceAsJson(myClientServiceImpl);
        String serverAsJson = SaveLoadUtils.Instance.getTransportServiceAsJson(myClientServiceImpl);

        TransportService loadedClient = null;
        TransportService loadedServer = null;

        try {
            loadedClient = SaveLoadUtils.Instance.getTransportServiceFromJson(clientAsJson);
            loadedServer = SaveLoadUtils.Instance.getTransportServiceFromJson(serverAsJson);
        }
        catch(Exception e){
            e.printStackTrace();
            fail("Failed while trying to load client and server from JSon:\n"+e.getMessage());
        }

        assertTrue(loadedClient.equals(myClientServiceImpl));
        assertTrue(loadedServer.equals(myClientServiceImpl));
    }

    /**
     * Helper method that will add 4 messages with 5 fields and 2 words each. The first two fields in the first word will be the ID fields
     */
    private void setUpDecoders() throws Exception{
        clientDecoder.addIdPeekInfo(0, 5);
        clientDecoder.addIdPeekInfo(5, 3);

        serverDecoder.addIdPeekInfo(0, 5);
        serverDecoder.addIdPeekInfo(5, 3);

        // Message1
        message1 = new StandardMessage();
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
        message2 = new StandardMessage();
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
        message3 = new StandardMessage();
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

        clientDecoder.addMessage(messageOneList, message1);
        clientDecoder.addMessage(messageTwoList, message2);
        clientDecoder.addMessage(messageThreeList, message3);
        serverDecoder.addMessage(messageOneList, message1);
        serverDecoder.addMessage(messageTwoList, message2);
        serverDecoder.addMessage(messageThreeList, message3);
    }

}