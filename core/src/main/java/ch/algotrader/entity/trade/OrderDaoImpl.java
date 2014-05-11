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

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;

import com.espertech.esper.collection.Pair;

import ch.algotrader.esper.EngineLocator;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderDaoImpl extends OrderDaoBase {

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Order> handleFindAllOpenOrders() throws Exception {

        return convertPairCollectionToOrderCollection(EngineLocator.instance().getBaseEngine().executeQuery("select * from OpenOrderWindow"));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Order> handleFindOpenOrdersByStrategy(String strategyName) throws Exception {

        return convertPairCollectionToOrderCollection(EngineLocator.instance().getBaseEngine().executeQuery("select * from OpenOrderWindow where strategy.name = '" + strategyName + "'"));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Order handleFindOpenOrderByIntId(String intId) throws Exception {

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EngineLocator.instance().getBaseEngine().executeSingelObjectQuery("select * from OpenOrderWindow where intId = '" + intId + "'"));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Order handleFindOpenOrderByRootIntId(String intId) throws Exception {

        String rootIntId = intId.split("\\.")[0];
        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EngineLocator.instance().getBaseEngine()
                .executeSingelObjectQuery("select * from OpenOrderWindow where intId like '" + rootIntId + ".%'"));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Order handleFindOpenOrderByExtId(String extId) throws Exception {

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EngineLocator.instance().getBaseEngine().executeSingelObjectQuery("select * from OpenOrderWindow where extId = " + extId));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Order> handleFindOpenOrdersByParentIntId(String parentIntId) throws Exception {

        return convertPairCollectionToOrderCollection(EngineLocator.instance().getBaseEngine()
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
