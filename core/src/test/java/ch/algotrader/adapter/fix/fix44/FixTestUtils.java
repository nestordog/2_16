/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.adapter.fix.fix44;

import quickfix.ConfigError;
import quickfix.DataDictionary;
import quickfix.InvalidMessage;
import quickfix.MessageUtils;
import quickfix.SessionID;
import quickfix.field.BeginString;
import quickfix.field.SenderCompID;
import quickfix.field.TargetCompID;
import quickfix.fix44.Message;
import quickfix.fix44.MessageFactory;

/**
 * FIX 4.4 test utilities.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FixTestUtils {

    private final static String FIELD_SEPARATOR = "\001";

    private final static MessageFactory MSG_FACTORY;
    private final static DataDictionary DATA_DICTIONARY;

    static {
        MSG_FACTORY = new MessageFactory();
        try {
            DATA_DICTIONARY = new DataDictionary("FIX44.xml");
        } catch (ConfigError configError) {
            throw new Error(configError);
        }
    }

    public static Message parseFix44Message(final String str, final DataDictionary dataDictionary) throws InvalidMessage {
        String raw;
        if (!str.contains(FIELD_SEPARATOR)) {
            raw = str.replaceAll("\\|", FIELD_SEPARATOR);
        } else {
            raw = str;
        }
        return (Message) MessageUtils.parse(MSG_FACTORY, dataDictionary != null ? dataDictionary : DATA_DICTIONARY, raw);
    }

    public static <T extends Message> T parseFix44Message(final String str, final DataDictionary dataDictionary, final Class<T> clazz) throws InvalidMessage {
        Message message = parseFix44Message(str, dataDictionary);
        if (!clazz.isInstance(message)) {
            throw new InvalidMessage("Expected message type: " + clazz.getName() + "; actual type: " + message.getClass().getName());
        }
        return clazz.cast(message);
    }

    public static <T extends Message> T parseFix44Message(final String str, final Class<T> clazz) throws InvalidMessage {
        return parseFix44Message(str, null, clazz);
    }

    public static Message parseFix44Message(final String str) throws InvalidMessage {
        return parseFix44Message(str, (DataDictionary) null);
    }

    public static SessionID fakeFix44Session() {
        return new SessionID(new BeginString("FIX.4.4"), new SenderCompID("test"), new TargetCompID("test"), "FAKE");
    }

}
