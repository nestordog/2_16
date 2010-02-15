package com.algoTrader.listener;

import com.algoTrader.ServiceLocator;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.StatementAwareUpdateListener;

public class RetrieveTickListener  implements StatementAwareUpdateListener {

    public void update(EventBean[] newEvents, EventBean[] oldEvents, EPStatement statement,
            EPServiceProvider epServiceProvider) {

        ServiceLocator.instance().getTickService().processSecuritiesOnWatchlist();
    }
}
