package com.chanse.messaging.messages;

import com.chanse.messaging.bitUtils.StandardUtils;
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
            if(word.isFieldDataHasChanged()){
                String wordAsBinaryString = word.getWordData().toString(2);
                this.messageAsSerialString.replace(bitsChecked.get(), wordAsBinaryString.length(), wordAsBinaryString);
            }
            bitsChecked.addAndGet(word.getNumberOfBytes() * 8);
        });
    }

    /**
     *
     */
    public void initializeMessageBinaryString(){
        AtomicInteger bitsChecked = new AtomicInteger();
        dataWords.stream().forEach( word -> {
            String wordAsBinaryString = StandardUtils.getBinaryStringFromBigInt(word.getWordData(), word.getNumberOfBytes() * 8);
            try{
                this.messageAsSerialString.replace(bitsChecked.get(), wordAsBinaryString.length(), wordAsBinaryString);
            }catch(Exception e){
                e.getMessage();
            }
            bitsChecked.addAndGet(word.getNumberOfBytes() * 8);
        });
    }

}
