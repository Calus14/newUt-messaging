package com.chanse.messaging.transport;

import com.chanse.messaging.exceptions.BaseMessagingException;
import com.chanse.messaging.exceptions.ConnectionException;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.msginterface.InterfaceDecoder;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of the TransportationService interface but in for the TCP protocol acting as a Server
 *
 * This service will behave as a client and specifcially look to bind to a ServerSocket at a given hostname/ipaddress
 * and port. If there is any known IO error after a sucessfull connection then the connection will be marked as a failure
 * and an event will be thrown.
 */
public class TCPServerTransportationService implements TransportService {

    @Getter
    @Setter
    protected Statuses myStatus = Statuses.NOTHING;

    @Getter
    @Setter
    protected Integer receivePort;

    //TODO allow for a serverTransportationService to accept multiple connects.
    @Getter
    @Setter
    protected Integer waitTimeoutSeconds = 10;

    @Getter
    @Setter
    protected InterfaceDecoder decoder;

    @Getter
    transient protected OutputStream outputStream;

    @Getter
    transient protected ServerSocket myServerSocket;

    @Getter
    transient protected Socket acceptedSocket;

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
        if(!isConnected || !isSetup())
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
            myServerSocket = new ServerSocket(receivePort);
            myStatus = Statuses.SET_UP;
        }
        catch(Exception exception ){
            // TODO Handle Exception logging
            exception.printStackTrace();
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
        if(!isSetup())
            throw new ConnectionException("Cannot connect the server socket because it has not been setup yet.");
        try{
            int secondsCounted = 0;
            acceptedSocket = null;

            while (secondsCounted < getWaitTimeoutSeconds() && acceptedSocket == null) {
                Thread acceptThread = new Thread(){
                    @Override
                    public void run() {
                        try {
                            acceptedSocket = myServerSocket.accept();
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                };
                acceptThread.start();
                Thread.sleep(1000);
                secondsCounted += 1;
                acceptThread.interrupt();
            }

            if(acceptedSocket == null) {
                throw new Exception("Error while waiting for client to connect to the server, unable to setup");
            }

            this.decoder.setInputStream(acceptedSocket.getInputStream());
            outputStream = acceptedSocket.getOutputStream();
            myStatus = Statuses.CONNECTED;
            isConnected = true;
        }
        catch(Exception exception ){
            // TODO Handle Exception logging
            exception.printStackTrace();
            myStatus = Statuses.FAILED_CONNECTION;
        }
    }

    @Override
    public void disconnect() throws IOException{
        if(myServerSocket != null)
            myServerSocket.close();
        if(acceptedSocket != null)
            acceptedSocket.close();
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
        if(obj instanceof TCPServerTransportationService == false)
            return false;

        TCPServerTransportationService other = (TCPServerTransportationService)obj;

        return (receivePort.equals(other.getReceivePort()) &&
                waitTimeoutSeconds.equals(other.getWaitTimeoutSeconds()) &&
                decoder.equals(other.getDecoder()) );
    }
}
