package com.algoTrader.util;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.ClassUtils;

import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.trade.Order;
import com.algoTrader.vo.OrderVO;

/**
 * provides conversion methods that would normaly be available through a DAO. However Order is non-persistent and does not have a DAO
 */
public class OrderUtil {

    public static OrderVO toOrderVO(final Order order) {

        OrderVO orderVO = new OrderVO();

        completeOrderVO(order, orderVO);

        return orderVO;
    }

    public static Collection<OrderVO> toOrderVOCollection(Collection<Order> entities) {

        Collection<OrderVO> result = new ArrayList<OrderVO>();
        result.addAll(CollectionUtils.collect(entities, new Transformer<Order, OrderVO>() {
            @Override
            public OrderVO transform(Order input) {
                return toOrderVO(input);
            }
        }));
        return result;
    }

    private static void completeOrderVO(Order order, OrderVO orderVO) {

        Security security = order.getSecurity();

        orderVO.setSide(order.getSide());
        orderVO.setQuantity(order.getQuantity());
        orderVO.setType(ClassUtils.getShortClassName(order.getClass()));
        orderVO.setName(security.toString());
        orderVO.setNumber(order.getNumber());
        orderVO.setDescription(order.getDescription());
    }
}
