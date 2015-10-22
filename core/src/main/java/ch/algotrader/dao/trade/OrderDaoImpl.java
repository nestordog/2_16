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
package ch.algotrader.dao.trade;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderImpl;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.util.DateTimeLegacy;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Repository // Required for exception translation
public class OrderDaoImpl extends AbstractDao<Order> implements OrderDao {

    public OrderDaoImpl(final SessionFactory sessionFactory) {

        super(OrderImpl.class, sessionFactory);
    }

    @Override
    public BigDecimal findLastIntOrderIdBySessionQualifier(String sessionQualifier) {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        return (BigDecimal) findUniqueObject(null, "Order.findLastIntOrderIdBySessionQualifier", QueryType.BY_NAME, new NamedParam("sessionQualifier", sessionQualifier));
    }

    @Override
    public BigDecimal findLastIntOrderIdByServiceType(String orderServiceType) {

        Validate.notEmpty(orderServiceType, "Order service type is empty");

        return (BigDecimal) findUniqueObject(null, "Order.findLastIntOrderIdByServiceType", QueryType.BY_NAME, new NamedParam("orderServiceType", orderServiceType));
    }

    @Override
    public List<Order> getDailyOrders() {

        LocalDate today = LocalDate.now();
        return find("Order.findDailyOrders", QueryType.BY_NAME, new NamedParam("curdate", DateTimeLegacy.toLocalDate(today)));
    }

    @Override
    public List<Order> getDailyOrdersByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        LocalDate today = LocalDate.now();
        return find("Order.findDailyOrdersByStrategy", QueryType.BY_NAME,
                new NamedParam("curdate", DateTimeLegacy.toLocalDate(today)), new NamedParam("strategyName", strategyName));
    }

    @Override
    public List<Long> findUnacknowledgedOrderIds() {

        return convertIds(findObjects(null, "Order.findUnacknowledgedOrderIds", QueryType.BY_NAME));
    }

    @Override
    public List<Order> findByIds(List<Long> ids) {

        Validate.notEmpty(ids, "Ids are empty");

        return this.find("Order.findByIds", QueryType.BY_NAME, new NamedParam("ids", ids));
    }

    @Override
    public Order findByIntId(final String intId) {

        Validate.notEmpty(intId, "IntId is empty");

        return findUnique("Order.findByIntId", QueryType.BY_NAME, new NamedParam("intId", intId));
    }

}
