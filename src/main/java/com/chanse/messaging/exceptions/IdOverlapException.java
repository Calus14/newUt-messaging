package com.chanse.messaging.exceptions;

/**
 * Exception that will be thrown whenever id's would overlap on an interface
 */
public class IdOverlapException extends BaseMessagingException {
    public IdOverlapException(String message){
        super(message);
    }
}
