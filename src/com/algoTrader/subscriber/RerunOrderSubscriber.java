package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Order;

public class RerunOrderSubscriber {

    public void update(Order order) {

        ServiceLocator.instance().getDispatcherService().getTransactionService().executeTransaction(order);
    }
}
