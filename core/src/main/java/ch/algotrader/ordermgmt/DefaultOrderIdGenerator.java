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
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.classic.Session;

import ch.algotrader.util.collection.IntegerMap;

public class DefaultOrderIdGenerator implements OrderIdGenerator {

    private final SessionFactory sessionFactory;
    private final IntegerMap<String> orderIds;

    public DefaultOrderIdGenerator(final SessionFactory sessionFactory) {

        Validate.notNull(sessionFactory, "SessionFactory is null");

        this.sessionFactory = sessionFactory;
        this.orderIds = new IntegerMap<>();
    }

    /**
     * Gets the next {@code orderId} for the specified {@code sessionQualifier}
     */
    @Override
    public synchronized String getNextOrderId(final String sessionQualifier) {

        Validate.notNull(sessionQualifier, "Session qualifier is null");

        if (!this.orderIds.containsKey(sessionQualifier)) {

            final Session currentSession = this.sessionFactory.getCurrentSession();
            final SQLQuery query = currentSession.createSQLQuery("select distinct convert(substring(o.int_id, length(a.session_qualifier) + 1), decimal) " +
                "as order_id from `order` as o join account as a on (o.account_fk = a.id) where a.session_qualifier = :sessionQualifier " +
                "order by order_id desc limit 1");
            query.setParameter("sessionQualifier", sessionQualifier);
            final BigDecimal orderId = (BigDecimal) query.uniqueResult();
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