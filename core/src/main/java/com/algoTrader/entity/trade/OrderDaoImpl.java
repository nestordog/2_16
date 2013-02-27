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
    protected Order handleFindOpenOrderByIntId(String intId) throws Exception {

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where intId = '" + intId + "'"));
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
        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where intId like '" + rootIntId + ".%'"));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Order handleFindOpenOrderByExtId(String extId) throws Exception {

        Pair<Order, Map<?, ?>> pair = ((Pair<Order, Map<?, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where extId = " + extId));
        if (pair == null) {
            return null;
        } else {
            return pair.getFirst();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<Order> handleFindOpenOrdersByParentIntId(String parentIntId) throws Exception {

        return convertPairCollectionToOrderCollection(EsperManager.executeQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where not algoOrder and parentOrder.intId = '" + parentIntId + "'"));
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
