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
package ch.algotrader.ordermgmt;

import java.math.BigDecimal;

import org.apache.commons.lang.Validate;

import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.util.collection.IntegerMap;

/**
 * Default {@link ch.algotrader.ordermgmt.OrderIdGenerator} implementation backed by
 * {@link ch.algotrader.dao.OrderDao#findLastIntOrderId(String)}..
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class DefaultOrderIdGenerator implements OrderIdGenerator {

    private final OrderDao orderDao;
    private final IntegerMap<String> orderIds;

    public DefaultOrderIdGenerator(final OrderDao orderDao) {

        Validate.notNull(orderDao, "orderDao is null");

        this.orderDao = orderDao;
        this.orderIds = new IntegerMap<>();
    }

    /**
     * Gets the next {@code orderId} for the specified {@code sessionQualifier}
     */
    @Override
    public synchronized String getNextOrderId(final String sessionQualifier) {

        Validate.notNull(sessionQualifier, "Session qualifier is null");

        if (!this.orderIds.containsKey(sessionQualifier)) {

            BigDecimal orderId = this.orderDao.findLastIntOrderId(sessionQualifier);
            this.orderIds.put(sessionQualifier, orderId != null ? orderId.intValue() : 0);
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
    public void setOrderId(final String sessionQualifier, final int orderId) {

        Validate.notNull(sessionQualifier, "Session qualifier is null");

        this.orderIds.put(sessionQualifier, orderId);
    }

}
