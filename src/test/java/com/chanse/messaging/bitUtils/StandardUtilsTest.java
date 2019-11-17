package com.chanse.messaging.bitUtils;

import com.chanse.messaging.Utils;
import com.chanse.messaging.messages.InterfaceMessage;
import jdk.internal.jline.internal.TestAccessible;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;

import static org.junit.Assert.*;

/**
 * Generated Test Class for Standard Bit Utils. Add your own tests with each additional method add
 */
public class StandardUtilsTest {

    // 00111100 11111111 10000000 00000000 00000001 10000001
    static final byte[] testInputStream1 = new byte[]{60, -1, -128, 0, 1, -127};

    @Before
    public void setUp() throws Exception {
        // Nothing
    }

    @After
    public void tearDown() throws Exception {
        // Nothing
    }

    /**
     * Verify the following:
     * 1) can peek at a single bit in a stream
     * 2) can peek at a series of bits in a stream
     * 3) can peek at a series of bits in a stream where the leading bit is 1 (sign extension case)
     * 4) Fails if we try to pass in a negative offset or negative length
     * 5) Fails if we try to peek past the length of the stream
     */
    @Test
    public void peekAtBits() {
        // TEST 1
        try {
            BigInteger expected1 = StandardUtils.peekAtBits(16, 1, testInputStream1);
            assertEquals(expected1.intValue(), 1);
            BigInteger expected0 = StandardUtils.peekAtBits(1, 1, testInputStream1);
            assertEquals(expected0.intValue(), 0);

            expected1 = StandardUtils.peekAtBits(47, 1, testInputStream1);
            assertEquals(expected1.intValue(), 1);
            expected0 = StandardUtils.peekAtBits(44, 1, testInputStream1);
            assertEquals(expected0.intValue(), 0);
        }
        catch(Exception e){
            fail(e.getMessage());
        }

        // TEST 2
        try {
            BigInteger expected15 = StandardUtils.peekAtBits(0, 6, testInputStream1);
            assertEquals(expected15.intValue(), 15);
            BigInteger expected30 = StandardUtils.peekAtBits(0, 7, testInputStream1);
            assertEquals(expected30.intValue(), 30);
            BigInteger expected499696 = StandardUtils.peekAtBits(0, 21, testInputStream1);
            assertEquals(expected499696.intValue(), 499696);
        }
        catch(Exception e){
            fail(e.getMessage());
        }

        // TEST 3
        try {
            BigInteger expected6 = StandardUtils.peekAtBits(39, 3, testInputStream1);
            assertEquals(expected6.intValue(), 6);
            BigInteger expected51 = StandardUtils.peekAtBits(4, 6, testInputStream1);
            assertEquals(expected51.intValue(), 51);
            BigInteger expected58720257 = StandardUtils.peekAtBits(14, 26, testInputStream1);
            assertEquals(expected58720257.intValue(), 58720257);
        }
        catch(Exception e){
            fail(e.getMessage());
        }

    }

    @Test
    public void getBinaryStringFromBigInt(){
        String test1 = "1000100101010";
        String test2 = "00000101001";
        String test3 = "0";
        String test4 = "0000000000000000000000000000000000000000000000000";
        assertEquals(test1, StandardUtils.getBinaryStringFromBigInt(new BigInteger(test1,2 ), test1.length()));
        assertEquals(test2, StandardUtils.getBinaryStringFromBigInt(new BigInteger(test2,2), test2.length()));
        assertEquals(test3, StandardUtils.getBinaryStringFromBigInt(new BigInteger(test3, 2), test3.length()));
        assertEquals(test4, StandardUtils.getBinaryStringFromBigInt(new BigInteger(test4, 2), test4.length()));
    }

    @Test
    public void fillMessageFromData() {
        for( int i = 0; i < 100; i ++){
            InterfaceMessage randomMessage = Utils.getRandomStandardMessage(20);
            System.out.println(randomMessage.getMessageAsSerialString());
        }
    }

    @Test
    public void fillWordFromData() {
    }

    @Test
    public void insertFieldIntoWord() {
    }
}