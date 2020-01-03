package com.chanse.messaging.msginterface;

import org.junit.Test;

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
    }

    @Test
    public void decodeMessages() {
    }

    @Test
    public void testDecodeMessages() {
    }
}