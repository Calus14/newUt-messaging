package com.chanse.messaging.exceptions;

public class DuplicateMessageIdException extends BaseMessagingException {
    public DuplicateMessageIdException(String message){
        super(message);
    }
}
