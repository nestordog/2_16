/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.fix;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ch.algotrader.util.Consts;
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
 *
 * @version $Revision$ $Date$
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

    public static DateFormat getSimpleDateTimeFormat() {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd-HH:mm:ss.SSS");
        f.setTimeZone(Consts.UTM);
        return f;
    }

    public static Date parseDateTime(final String s) throws ParseException {
        return getSimpleDateTimeFormat().parse(s);
    }

}
