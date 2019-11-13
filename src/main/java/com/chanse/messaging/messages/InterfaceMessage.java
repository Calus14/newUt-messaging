package com.chanse.messaging.messages;

import com.chanse.messaging.words.InterfaceDataWord;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * The InterfaceMessage class represents the most basic interface message which is a compilation of "Fields"
 * which make up "words" that can be flattened into json for saving at a later point and serialized into
 * binary as well.
 *
 * This is just the abstract class that can be re-used later by more specific classes such as "Repeating Messages"
 * "protobuffer messages" "1553 messages" etc.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class InterfaceMessage{

    // Each message will hold a static reference to memory so that it will only calculate the given binary string
    // if a field has been updated on it.
    protected StringBuffer messageAsSerialString;

    // What do we call this message
    protected String messageName;

    // Flag to decide if it needs to recalculate the message to serial string
    protected boolean dataHasChanged;

    // The Individual Data Words because were cool like that
    protected List<InterfaceDataWord> dataWords;

    // Method each message type will need to implement for how to find out its binary string. Different message
    // will do this differently... ie a protobuffer vs something that is simulating interlaced video.... SPOOOOKY
    public abstract void recalculateMessageAsBinaryString();

}
