/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.starter;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import ch.algotrader.ServiceLocator;
import quickfix.FileStoreFactory;
import quickfix.MessageStore;
import quickfix.MessageStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;

/**
 * Starter Class for printing FIX session information and optionally resetting a session.
 * <p>
 * Usage: {@code ReferenceDataStarter securityFamilyId1 securityFamilyId2}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FixSessionInfoStarter {

    public static void main(String... args) throws Exception {

        ServiceLocator instance = ServiceLocator.instance();
        instance.init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        try {
            SessionSettings sessionSettings = instance.getContext().getBean("fixSessionSettings", SessionSettings.class);
            Iterator<SessionID> it = sessionSettings.sectionIterator();
            System.out.println("Available sessions:");
            System.out.println("-------------------");

            MessageStoreFactory messageStoreFactory = new FileStoreFactory(sessionSettings);

            Map<String, MessageStore> sessionIDMap = new HashMap<>();
            while (it.hasNext()) {
                SessionID sessionID = it.next();
                System.out.println(sessionID);
                MessageStore messageStore = messageStoreFactory.create(sessionID);
                System.out.println("  Creation time: " + messageStore.getCreationTime());
                System.out.println("  Next sender MsgSegNum: " + messageStore.getNextSenderMsgSeqNum());
                System.out.println("  Next target MsgSegNum: " + messageStore.getNextTargetMsgSeqNum());

                sessionIDMap.put(sessionID.getSessionQualifier(), messageStore);
            }

            if (args.length >= 2) {
                String command = args[0];
                String sessionQualifier = args[1];
                MessageStore messageStore = sessionIDMap.get(sessionQualifier);
                if (messageStore != null) {
                    if (command.equals("reset")) {
                        messageStore.reset();
                    } else if (command.equals("set") && args.length >= 3) {
                        messageStore.setNextSenderMsgSeqNum(Integer.parseInt(args[2]));
                        System.out.println("Set " + sessionQualifier + " sender MsgSegNum to " +  messageStore.getNextSenderMsgSeqNum());
                        if (args.length >= 4) {
                            messageStore.setNextTargetMsgSeqNum(Integer.parseInt(args[3]));
                            System.out.println("Set " + sessionQualifier + " target MsgSegNum to " +  messageStore.getNextTargetMsgSeqNum());
                        }
                    }
                } else {
                    System.err.println("Unknown session qualifier: " + sessionQualifier);
                }
            }
        } finally {
            instance.shutdown();
        }
    }
}
