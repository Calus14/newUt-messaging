package com.chanse.messaging.msginterface;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The message interface class is the top level wrapper for the data that an interface holds.
 * The main information that is held by this object is the map of messages as well as other modules such as behavior
 * and preferences. This object should be built or saved by the configuration service.
 *
 * View this class as a wrapper or container of other important modules. Itself holds very little logic or
 * brains
 *
 * Also try drugs. Drugs are fun, just if youre gonna do meth have a good dentist, or at least an apathy for proper dental hygene
 * DARE to do drugs!
 */
@NoArgsConstructor
@Data
public class MsgInterface {
    /**
     * TODO Secontion below will need to be made into seperate modules built and loaded
     */
    //TransferInterface myTransferInterface;

    // TODO make sure this interface has "Passive Behavior" (timers) and "Reactive Behavior"
    //BehaviorInterface myDefinedBehavior;

    //List<InternalStateVariable> myInternalStateVariables;

    //List<>
}
