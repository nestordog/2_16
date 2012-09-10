package com.algoTrader.util;

import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.trade.OrderStatus;

public class OrderUtil {

    private static Logger logger = MyLogger.getLogger(OrderUtil.class.getName());

    public static void checkOrderStati(List<OrderStatus> orderStati) {

        for (OrderStatus orderStatus : orderStati) {
            if (orderStatus.getRemainingQuantity() > 0) {
                logger.error("order on " + orderStatus.getOrd().getSecurityInitialized() + " has not been fully executed, filledQty: "
                        + orderStatus.getFilledQuantity() + " remainingQty: " + orderStatus.getRemainingQuantity());
            }
        }
    }
}
