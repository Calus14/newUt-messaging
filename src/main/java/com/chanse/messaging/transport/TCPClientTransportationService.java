package com.chanse.messaging.transport;


import com.chanse.messaging.exceptions.BaseMessagingException;
import com.chanse.messaging.exceptions.ConnectionException;
import com.chanse.messaging.msginterface.InterfaceDecoder;
import lombok.Setter;
import lombok.Getter;
import com.chanse.messaging.messages.InterfaceMessage;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of the TransportationService interface but in for the TCP protocol acting as a client
 *
 * This service will behave as a client and specifcially look to bind to a ServerSocket at a given hostname/ipaddress
 * and port. If there is any known IO error after a sucessfull connection then the connection will be marked as a failure
 * and an event will be thrown.
 */
public class TCPClientTransportationService implements TransportService {

    @Getter
    @Setter
    protected Statuses myStatus = Statuses.NOTHING;

    @Getter
    @Setter
    protected Integer receivePort;

    @Getter
    @Setter
    protected Integer sendPort;

    @Getter
    @Setter
    protected String sendHostAddress;

    @Getter
    @Setter
    protected InterfaceDecoder decoder;

    @Getter
    transient protected OutputStream outputStream;

    @Getter
    transient protected Socket mySocket;

    transient private boolean isConnected = false;

    @Override
    public void sendMessage(byte[] msg) {
        try{
            outputStream.write(msg);
        }
        catch(IOException e)
        {
            //TODO Handle exception
            e.printStackTrace();
            isConnected = false;
        }
    }

    @Override
    public List<InterfaceMessage> receiveMessages() {
        if(!isConnected)
            return null;

        List<InterfaceMessage> receivedMessages = new ArrayList<>();
        try {
            receivedMessages = decoder.decodeMessages();
        }
        catch(IOException | BaseMessagingException e){
            //TODO Handle a read error and push an error event to the error logging service
            e.printStackTrace();
            isConnected = false;
        }
        return receivedMessages;
    }

    @Override
    public boolean isSetup(){
        return decoder != null;
    }

    @Override
    public void setup() {
        myStatus = Statuses.SETTING_UP;
        try{
            mySocket = new Socket();
            mySocket.bind(new InetSocketAddress(receivePort));

            myStatus = Statuses.SET_UP;
        }
        catch(IOException ioexception){
            // TODO Handle Exception logging
            ioexception.printStackTrace();
            myStatus = Statuses.FAILED_SETUP;
        }
    }

    @Override
    public boolean isConnected() {
        return isConnected;
    }

    @Override
    public void connect() throws ConnectionException {
        myStatus = Statuses.CONNECTING;
       try{
           mySocket.connect(new InetSocketAddress(sendHostAddress, sendPort));
           decoder.setInputStream(mySocket.getInputStream());
           outputStream = mySocket.getOutputStream();
           isConnected = true;
           myStatus = Statuses.CONNECTED;
       }
       catch(IOException ioexception){
           // TODO Handle Exception logging
           ioexception.printStackTrace();
           myStatus = Statuses.FAILED_CONNECTION;
       }
    }

    @Override
    public void disconnect() throws IOException{
        mySocket.close();
        myStatus = Statuses.DISCONNECTED;
    }

    @Override
    public String getStatus() {
        return myStatus.toString();
    }

    /**
     * States if they setup of the transport service is equal. Not if the connections activity and streams are
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj){
        if(obj instanceof TCPClientTransportationService == false)
            return false;

        TCPClientTransportationService other = (TCPClientTransportationService)obj;

        return (receivePort.equals(other.getReceivePort()) &&
                sendPort.equals(other.getSendPort()) &&
                sendHostAddress.equals(other.getSendHostAddress()) &&
                decoder.equals(other.getDecoder()) );
    }

}
