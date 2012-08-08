package com.algoTrader.entity.trade;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.esper.EsperManager;
import com.espertech.esper.collection.Pair;

public class OrderDaoImpl extends OrderDaoBase {

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Order> handleFindAllOpenOrders() throws Exception {

        return convertPairCollectionToOrderCollection(EsperManager.executeQuery(StrategyImpl.BASE, "select * from OpenOrderWindow"));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Order handleFindOpenOrderByNumber(int number) throws Exception {

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where number = " + number));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Order> handleFindOpenOrdersByParentNumber(int parentNumber) throws Exception {

        return convertPairCollectionToOrderCollection(EsperManager.executeQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where not algoOrder and parentOrder.number = " + parentNumber));
    }

    @SuppressWarnings("unchecked")
    @Override
    protected int handleFindOpenOrderCountByStrategySecurityAndAlgoOrder(String strategyName, int securityId, boolean algoOrder) throws Exception {

        return ((Long)((Map<String,?>)EsperManager.executeSingelObjectQuery(StrategyImpl.BASE,
                "select count(number) as cnt from OpenOrderWindow as openOrderWindow" +
                " where openOrderWindow.security.id = " + securityId +
                " and openOrderWindow.strategy.name = '" + strategyName + "'" +
                " and openOrderWindow.algoOrder = " + algoOrder)).get("cnt")).intValue();
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
