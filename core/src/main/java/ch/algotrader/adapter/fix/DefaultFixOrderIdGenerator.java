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
package ch.algotrader.adapter.fix;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;

import ch.algotrader.service.LookupService;
import ch.algotrader.util.collection.IntegerMap;
import quickfix.SessionID;

/**
 * File backed implementation of {@link FixOrderIdGenerator}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
class DefaultFixOrderIdGenerator implements FixOrderIdGenerator {

    private final LookupService lookupService;
    private final IntegerMap<String> orderIds;

    public DefaultFixOrderIdGenerator(final LookupService lookupService) {

        Validate.notNull(lookupService, "LookupService is null");

        this.lookupService = lookupService;
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

        BigDecimal orderId = this.lookupService.getLastIntOrderId(sessionQualifier);
        this.orderIds.put(sessionQualifier, orderId != null ? orderId.intValue() : 0);
    }
}
