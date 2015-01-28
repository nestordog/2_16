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
package ch.algotrader.entity.trade;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.Validate;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.type.IntegerType;
import org.springframework.stereotype.Repository;

import com.espertech.esper.collection.Pair;

import ch.algotrader.enumeration.QueryType;
import ch.algotrader.esper.Engine;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

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
    public List<Integer> findUnacknowledgedOrderIds() {

        return (List<Integer>) findObjects(null, "Order.findUnacknowledgedOrderIds", QueryType.BY_NAME);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Order> findByIds(List<Integer> ids) {

        Validate.notEmpty(ids, "Ids are empty");

        Query query = this.prepareQuery(null, "Order.findByIds", QueryType.BY_NAME);
        query.setParameterList("ids", ids, IntegerType.INSTANCE);

        return query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findAllOpenOrders() {

        return this.convertPairCollectionToOrderCollection(this.serverEngine.executeQuery("select * from OpenOrderWindow"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findOpenOrdersByStrategy(String strategyName) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return this.convertPairCollectionToOrderCollection(this.serverEngine.executeQuery("select * from OpenOrderWindow where strategy.name = '" + strategyName + "'"));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findOpenOrdersByStrategyAndSecurity(String strategyName, int securityId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return this.convertPairCollectionToOrderCollection(this.serverEngine
                .executeQuery("select * from OpenOrderWindow where strategy.name = '" + strategyName + "' and security.id = " + securityId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findOpenOrderByIntId(String intId) {

        Validate.notEmpty(intId, "intId is empty");

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) this.serverEngine.executeSingelObjectQuery("select * from OpenOrderWindow where intId = '" + intId + "'"));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findOpenOrderByRootIntId(String intId) {

        Validate.notEmpty(intId, "intId is empty");

        String rootIntId = intId.split("\\.")[0];
        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) this.serverEngine
                .executeSingelObjectQuery("select * from OpenOrderWindow where intId like '" + rootIntId + ".%'"));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Order findOpenOrderByExtId(String extId) {

        Validate.notEmpty(extId, "extId is empty");

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) this.serverEngine.executeSingelObjectQuery("select * from OpenOrderWindow where extId = '" + extId + "'"));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<Order> findOpenOrdersByParentIntId(String parentIntId) {

        Validate.notEmpty(parentIntId, "parentIntId is empty");

        return this.convertPairCollectionToOrderCollection(this.serverEngine
                .executeQuery("select * from OpenOrderWindow where not algoOrder and parentOrder.intId = '" + parentIntId + "'"));
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
