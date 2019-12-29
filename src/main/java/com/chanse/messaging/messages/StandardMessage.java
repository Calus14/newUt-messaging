package com.chanse.messaging.messages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class StandardMessage extends InterfaceMessage {

    /**
     * Assumption: The list of dataWords are in order and themselfs correctly set in binary
     * Strategy: Iterate down the list of words. If the word has field data changed grab its
     * string representation and replace it in our own binary string.
     * Advantage: This only touches the words that has had data change, does not re-calucalte any words
     *  and is simple to debug
     */
    @Override
    public void recalculateMessageAsBinaryString() {
        // Atomic must be used because there must be a threadsafe operation for stream lambdas
        AtomicInteger bitsChecked = new AtomicInteger();
        dataWords.stream().forEach( word -> {

            String wordAsBinaryString = word.getWordDataAsBinaryString();
            this.messageAsSerialString.replace(bitsChecked.get(), bitsChecked.get() + wordAsBinaryString.length(), wordAsBinaryString);

            bitsChecked.addAndGet(word.getNumberOfBytes() * 8);
        });
    }

    /**
     * Method that will initialize the binary string for the message. Essentially clearing the string and setting it to
     * whatever data each word holds
     */
    public void initializeMessageBinaryString(){
        this.messageAsSerialString = new StringBuffer();

        dataWords.stream().forEach( word -> {
            this.messageAsSerialString.append(word.getWordDataAsBinaryString());
        });
    }

}
