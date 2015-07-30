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
package ch.algotrader.dao.trade;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.LongType;
import org.springframework.stereotype.Repository;

import com.espertech.esper.collection.Pair;

import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.OrderImpl;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.esper.Engine;
import ch.algotrader.hibernate.AbstractDao;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class OrderDaoImpl extends AbstractDao<Order> implements OrderDao {

    private final Engine serverEngine;

    public OrderDaoImpl(final SessionFactory sessionFactory, final Engine serverEngine) {

        super(OrderImpl.class, sessionFactory);
        Validate.notNull(serverEngine, "Engine is null");
        this.serverEngine = serverEngine;
    }

    @Override
    public BigDecimal findLastIntOrderId(String sessionQualifier) {

        Validate.notEmpty(sessionQualifier, "Session qualifier is empty");

        return (BigDecimal) findUniqueObject(null, "Order.findLastIntOrderId", QueryType.BY_NAME, new NamedParam("sessionQualifier", sessionQualifier));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Long> findUnacknowledgedOrderIds() {

        return convertIds(findObjects(null, "Order.findUnacknowledgedOrderIds", QueryType.BY_NAME));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Order> findByIds(List<Long> ids) {

        Validate.notEmpty(ids, "Ids are empty");

        Query query = this.prepareQuery(null, "Order.findByIds", QueryType.BY_NAME);
        query.setParameterList("ids", ids, LongType.INSTANCE);

        return query.list();
    }

    @Override
    public Order findByIntId(final String intId) {

        Validate.notEmpty(intId, "IntId is empty");

        return findUnique("Order.findByIntId", QueryType.BY_NAME, new NamedParam("intId", intId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findAllOpenOrders() {

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            return this.convertPairCollectionToOrderCollection(this.serverEngine.executeQuery("select * from OpenOrderWindow"));
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findOpenOrdersByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            return this.convertPairCollectionToOrderCollection(this.serverEngine.executeQuery("select * from OpenOrderWindow where strategy.name = '" + strategyName + "'"));
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findOpenOrdersByStrategyAndSecurity(String strategyName, long securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            return this.convertPairCollectionToOrderCollection(this.serverEngine.executeQuery("select * from OpenOrderWindow where strategy.name = '" + strategyName + "' and security.id = "
                    + securityId));
        } else {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findOpenOrderByIntId(String intId) {

        Validate.notEmpty(intId, "intId is empty");

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) this.serverEngine.executeSingelObjectQuery("select * from OpenOrderWindow where intId = '" + intId + "'"));
            if (pair != null) {
                return pair.getFirst();
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findOpenOrderByRootIntId(String intId) {

        Validate.notEmpty(intId, "intId is empty");

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            String rootIntId = intId.split("\\.")[0];
            Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) this.serverEngine.executeSingelObjectQuery("select * from OpenOrderWindow where intId like '" + rootIntId + ".%'"));
            if (pair != null) {
                return pair.getFirst();
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findOpenOrderByExtId(String extId) {

        Validate.notEmpty(extId, "extId is empty");

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) this.serverEngine.executeSingelObjectQuery("select * from OpenOrderWindow where extId = '" + extId + "'"));
            if (pair != null) {
                return pair.getFirst();
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findOpenOrdersByParentIntId(String parentIntId) {

        Validate.notEmpty(parentIntId, "parentIntId is empty");

        if (this.serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            return this.convertPairCollectionToOrderCollection(this.serverEngine.executeQuery("select * from OpenOrderWindow where not algoOrder and parentOrder.intId = '" + parentIntId + "'"));
        } else {
            return Collections.emptyList();
        }
    }

    private Collection<Order> convertPairCollectionToOrderCollection(Collection<Pair<Order, Map<?, ?>>> pairs) {

        return CollectionUtils.collect(pairs, new Transformer<Pair<Order, Map<?, ?>>, Order>() {
            @Override
            public Order transform(Pair<Order, Map<?, ?>> pair) {
                return pair.getFirst();
            }
        });
    }

}
