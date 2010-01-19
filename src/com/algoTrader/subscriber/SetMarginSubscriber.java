package com.algoTrader.subscriber;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Transaction;

public class SetMarginSubscriber {

    public void update(Transaction transaction) {

        ServiceLocator.instance().getActionService().setMargins();
    }
}
