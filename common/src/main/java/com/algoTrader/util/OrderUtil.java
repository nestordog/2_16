package com.algoTrader.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.ClassUtils;
import org.apache.commons.lang.StringUtils;

import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.enumeration.Status;
import com.algoTrader.vo.OrderVO;
import com.espertech.esper.collection.Pair;

/**
 * provides conversion methods that would normaly be available through a DAO. However Order is non-persistent and does not have a DAO
 */
public class OrderUtil {

    public static OrderVO toOrderVO(final Pair<Order, Map<String, ?>> pair) {

        OrderVO orderVO = new OrderVO();

        completeOrderVO(pair, orderVO);

        return orderVO;
    }

    public static Collection<OrderVO> toOrderVOCollection(Collection<Pair<Order, Map<String, ?>>> pairs) {

        Collection<OrderVO> result = new ArrayList<OrderVO>();
        result.addAll(CollectionUtils.collect(pairs, new Transformer<Pair<Order, Map<String, ?>>, OrderVO>() {
            @Override
            public OrderVO transform(Pair<Order, Map<String, ?>> pair) {
                return toOrderVO(pair);
            }
        }));
        return result;
    }

    private static void completeOrderVO(Pair<Order, Map<String, ?>> pair, OrderVO orderVO) {

        Order order = pair.getFirst();
        Map<String, ?> map = pair.getSecond();
        Security security = order.getSecurity();

        orderVO.setSide(order.getSide());
        orderVO.setQuantity(order.getQuantity());
        orderVO.setType(StringUtils.substringBefore(ClassUtils.getShortClassName(order.getClass()), "OrderImpl"));
        orderVO.setName(security.toString());
        orderVO.setNumber(order.getNumber());
        orderVO.setStatus((Status) map.get("status"));
        orderVO.setFilledQuantity((Long) map.get("filledQuantity"));
        orderVO.setRemainingQuantity((Long) map.get("remainingQuantity"));
        orderVO.setDescription(order.getDescription());
    }
}
