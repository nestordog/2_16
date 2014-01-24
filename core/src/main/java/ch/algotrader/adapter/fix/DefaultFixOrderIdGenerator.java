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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;
import ch.algotrader.util.collection.IntegerMap;
import quickfix.FileUtil;
import quickfix.SessionID;

/**
 * File backed implementation of {@link FixOrderIdGenerator}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class DefaultFixOrderIdGenerator implements FixOrderIdGenerator {

    private static Logger logger = MyLogger.getLogger(DefaultFixOrderIdGenerator.class.getName());

    private final File rootDir;
    private final IntegerMap<String> orderIds;

    public DefaultFixOrderIdGenerator() {
        this.rootDir = new File("log");
        this.orderIds = new IntegerMap<String>();
    }

    /**
     * Gets the next {@code orderId} for the specified {@code account}
     */
    @Override
    public synchronized String getNextOrderId(SessionID sessionID) {

        Validate.notNull(sessionID, "Session id may not be null");

        String sessionQualifier = sessionID.getSessionQualifier();
        if (!this.orderIds.containsKey(sessionQualifier)) {
            initOrderId(sessionID);
        }

        int rootOrderId = this.orderIds.increment(sessionQualifier, 1);
        return sessionQualifier.toLowerCase() + rootOrderId + ".0";
    }

    /**
     *  gets the currend orderIds for all active sessions
     */
    @Override
    public IntegerMap<String> getOrderIds() {

        return this.orderIds;
    }

    /**
     * sets the orderId for the defined session (will be incremented by 1 for the next order)
     */
    @Override
    public void setOrderId(String sessionQualifier, int orderId) {

        Validate.notNull(sessionQualifier, "Session identifier may not be null");

        this.orderIds.put(sessionQualifier, orderId);
    }

    /**
     * gets the last orderId from the fix message log
     */
    private void initOrderId(SessionID sessionID) {

        String sessionQualifier = sessionID.getSessionQualifier();

        File file = new File(rootDir, FileUtil.sessionIdFileName(sessionID) + ".messages.log");
        StringBuilder sb = new StringBuilder();

        try {
            RandomAccessFile fileHandler = new RandomAccessFile(file, "r");
            try {
                long fileLength = file.length() - 1;

                byte[] bytes = new byte[4];
                byte[] clOrdId = new byte[] { 0x1, 0x31, 0x31, 0x3D };
                long pointer;
                for (pointer = fileLength; pointer != -1; pointer--) {
                    fileHandler.seek(pointer);
                    fileHandler.read(bytes);
                    if (Arrays.equals(bytes, clOrdId)) {
                        break;
                    }
                }

                if (pointer == -1) {
                    this.orderIds.put(sessionQualifier, 1); // no last orderId
                    return;
                }

                for (; pointer != fileLength; pointer++) {
                    int readByte = fileHandler.readByte();
                    if (readByte == 0x1) {
                        break;
                    }
                    sb.append((char) readByte);
                }

            } finally {
                try {
                    fileHandler.close();
                } catch (IOException e) {
                    logger.error("problem finding last orderId", e);
                }
            }
        } catch (IOException e) {
            logger.error("problem finding last orderId", e);
        }

        // strip out the session qualifier
        String value = sb.toString().replaceAll("[a-z]", "");

        // strip out child order number
        if (value.contains(".")) {
            value = value.split("\\.")[0];
        }

        this.orderIds.put(sessionQualifier, Integer.valueOf(value));
    }
}
