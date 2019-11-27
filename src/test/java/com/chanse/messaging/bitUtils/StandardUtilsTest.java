package com.chanse.messaging.bitUtils;

import com.chanse.messaging.Utils;
import com.chanse.messaging.fields.InterfaceDataField;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.words.InterfaceDataWord;
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

    /**
     * Test generate a number of random messages. Then gets the message as a byte array and tries to re-create the message
     * from the raw data. Verifies that the binary string it started with matches the binary string it reads from bytes.
     */
    @Test
    public void fillMessageFromData() {
        for( int i = 0; i < 100; i ++){
            InterfaceMessage randomMessage = Utils.getRandomStandardMessage(20);
            if(randomMessage.getMessageAsSerialString().length() % 8 != 0)
                fail("While Constructing a random message, found that the length was not divisible into bytes");

            // Not using BigInteger here because leading 0's will be concatinated
            String oldMessageAsString = randomMessage.getMessageAsSerialString().toString();
            byte[] randomMessageAsBytes = StandardUtils.getByteArrayFromBinaryString(oldMessageAsString);

            // Clear the message data, setting the randomMessages data back to 0's
            StringBuffer cleanBuffer = new StringBuffer();
            for(int s = 0; s<randomMessage.getMessageAsSerialString().length(); s++)
                cleanBuffer.append("0");

            try{
                StandardUtils.fillMessageFromData(randomMessage, randomMessageAsBytes);
            }
            catch(Exception e){
                fail("Failed while filling message from data with message "+e.getMessage());
            }

            assertEquals(randomMessage.getMessageAsSerialString(), oldMessageAsString);
        }
    }

    @Test
    public void fillWordFromData() {
    }


    /**
     * Not the best test but make 100 random words 10 bytes long. For each field in the word set a random value
     * on the field, and insert it into that word. then verify that the word has that exact string
     */
    @Test
    public void insertFieldIntoWord() {
        for(int i = 0; i<100; i++) {
            InterfaceDataWord testWord = Utils.getRandomStandardDataWord(10);
            for(InterfaceDataField field : testWord.getDataFields()){

                StringBuilder randomFieldValueBuilder = new StringBuilder();
                for(int s = 0; s<field.getBitLength(); s++){
                    if( Utils.myRandom.nextBoolean() )
                        randomFieldValueBuilder.append('1');
                    else
                        randomFieldValueBuilder.append('0');
                }

                field.setDataBinaryString(randomFieldValueBuilder.toString());

                try{
                    StandardUtils.insertFieldIntoWord(testWord, field);
                }catch(Exception e) {
                    fail(e.getMessage());
                }

                String wordsFieldValue = testWord.getWordDataAsBinaryString().substring((int)field.getBitOffset(), (int)field.getBitLength());
                assertEquals(wordsFieldValue, field.getDataBinaryString());
            }
        }
    }
}