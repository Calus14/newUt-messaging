package com.chanse.messaging.utils;

import com.chanse.messaging.Utils;
import com.chanse.messaging.messages.InterfaceMessage;
import com.chanse.messaging.messages.StandardMessage;
import com.chanse.messaging.words.InterfaceDataWord;
import com.chanse.messaging.words.StandardDataWord;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SaveLoadUtilsTest {

    /**
     * Test to verify that loading from known GSON File will create the correct Message, files named for what they should actually be
     */
    @Test
    public void loadMessagesFromFile(){
        String filePath = "src/test/resources/testSaveFile1.json";
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
}
