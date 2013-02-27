package com.algoTrader.entity.trade;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.strategy.Allocation;
import com.algoTrader.util.MyLogger;

public class DistributingOrderImpl extends DistributingOrder {

    private static final Logger logger = MyLogger.getLogger(DistributingOrderImpl.class.getName());

    private static final long serialVersionUID = -3256407214793599390L;

    @Override
    public void validate() throws OrderValidationException {

        // do nothing
    }

    @Override
    public List<Order> getInitialOrders() {

        List<Order> orders = new ArrayList<Order>();
        long totalQuantity = 0;
        StringBuffer buffer = new StringBuffer();
        for (Allocation allocation : getAllocations()) {

            // qty proportional to allocation
            long quantity = Math.round(getQuantity() * allocation.getValue());
            totalQuantity += quantity;

            // create the market order
            Order order = MarketOrder.Factory.newInstance();
            order.setSecurity(this.getSecurity());
            order.setStrategy(this.getStrategy());
            order.setSide(this.getSide());
            order.setQuantity(quantity);
            order.setAccount(allocation.getAccount());

            // associate the childOrder with the parentOrder(this)
            order.setParentOrder(this);

            orders.add(order);

            buffer.append(order.getAccount() + "(" + order.getQuantity() + ") ");
        }

        // adjust quantity in case of rounding issue
        if (totalQuantity != getQuantity()) {
            logger.info("adjusting totalQuantity of " + toString() + " from " + getQuantity() + " to " + totalQuantity);
            setQuantity(totalQuantity);
        }

        logger.info("created child orders for " + toString() + " " + buffer.toString());

        return orders;
    }
}
