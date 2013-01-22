package com.algoTrader.entity.trade;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.enumeration.Status;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.vo.OrderStatusVO;
import com.espertech.esper.collection.Pair;

public class OrderStatusDaoImpl extends OrderStatusDaoBase {

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<OrderStatusVO> handleFindAllOrderStati() throws Exception {

        Collection<Pair<Order, Map<String, ?>>> pairs = EsperManager.executeQuery(StrategyImpl.BASE, "select * from OpenOrderWindow");
        return convertPairCollectionToOrderStatusVOCollection(pairs);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected OrderStatusVO handleFindOrderStatusByExtId(int extId) throws Exception {

        Pair<Order, Map<String, ?>> pair = (Pair<Order, Map<String, ?>>) EsperManager.executeSingelObjectQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where extId = " + extId);
        return convertPairToOrderStatusVO(pair);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected Collection<OrderStatusVO> handleFindOrderStatiByStrategy(String strategyName) throws Exception {

        Collection<Pair<Order, Map<String, ?>>> pairs = EsperManager.executeQuery(StrategyImpl.BASE, "select * from OpenOrderWindow where strategy.name = '" + strategyName + "'");
        return convertPairCollectionToOrderStatusVOCollection(pairs);
    }

    private Collection<OrderStatusVO> convertPairCollectionToOrderStatusVOCollection(Collection<Pair<Order, Map<String, ?>>> pairs) {

        return CollectionUtils.collect(pairs, new Transformer<Pair<Order, Map<String, ?>>, OrderStatusVO>() {
            @Override
            public OrderStatusVO transform(Pair<Order, Map<String, ?>> pair) {
                return convertPairToOrderStatusVO(pair);
            }
        });
    }

    private OrderStatusVO convertPairToOrderStatusVO(Pair<Order, Map<String, ?>> pair) {

        Order order = pair.getFirst();
        Map<String, ?> map = pair.getSecond();

        OrderStatusVO orderStatusVO = new OrderStatusVO();
        orderStatusVO.setSide(order.getSide());
        orderStatusVO.setQuantity(order.getQuantity());
        orderStatusVO.setType(StringUtils.substringBefore(ClassUtils.getShortClassName(order.getClass()), "OrderImpl"));
        orderStatusVO.setName(order.getSecurity().toString());
        orderStatusVO.setMarketChannel(order.getMarketChannel() != null ? order.getMarketChannel().getValue() : "");
        orderStatusVO.setNumber(order.getExtId());
        orderStatusVO.setStatus((Status) map.get("status"));
        orderStatusVO.setFilledQuantity((Long) map.get("filledQuantity"));
        orderStatusVO.setRemainingQuantity((Long) map.get("remainingQuantity"));
        orderStatusVO.setDescription(order.getDescription());

        return orderStatusVO;
    }
}
