package com.algoTrader.service.ib;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.SubscribeTickVO;
import com.algoTrader.vo.UnsubscribeTickVO;
import com.ib.client.Contract;

public class IBMarketDataServiceImpl extends IBMarketDataServiceBase implements DisposableBean {

    private static final long serialVersionUID = -4704799803078842628L;
    private static Logger logger = MyLogger.getLogger(IBMarketDataServiceImpl.class.getName());
    private static IBClient client;

    private @Value("${simulation}") boolean simulation;
    private @Value("${ib.genericTickList}") String genericTickList;

    @Override
    protected void handleInit() throws Exception {

        client = getIBClientFactory().getDefaultClient();
    }

    @Override
    protected void handleInitSubscriptions() {

        if (client != null
                && (client.getMessageHandler().getState().getValue() >= ConnectionState.LOGGED_ON.getValue())
                && !client.getMessageHandler().isRequested() && !this.simulation) {

            client.getMessageHandler().setRequested(true);
            client.getMessageHandler().setState(ConnectionState.SUBSCRIBED);

            super.handleInitSubscriptions();
        }
    }

    @Override
    protected void handleExternalSubscribe(Security security) throws Exception {

        if (client.getMessageHandler().getState().getValue() < ConnectionState.LOGGED_ON.getValue()) {
            throw new IBMarketDataServiceException("IB is not logged on to subscribe " + security);
        }

        // create the SubscribeTickEvent (must happen before reqMktData so that Esper is ready to receive marketdata)
        int tickerId = IBIdGenerator.getInstance().getNextRequestId();
        Tick tick = Tick.Factory.newInstance();
        tick.setSecurity(security);

        SubscribeTickVO subscribeTickEvent = new SubscribeTickVO();
        subscribeTickEvent.setTick(tick);
        subscribeTickEvent.setTickerId(tickerId);

        EsperManager.sendEvent(StrategyImpl.BASE, subscribeTickEvent);

        // requestMarketData from IB
        Contract contract = IBUtil.getContract(security);

        client.reqMktData(tickerId, contract, this.genericTickList, false);

        logger.debug("request " + tickerId + " for : " + security);
    }

    @Override
    protected void handleExternalUnsubscribe(Security security) throws Exception {

        if (!client.getMessageHandler().getState().equals(ConnectionState.SUBSCRIBED)) {
            throw new IBMarketDataServiceException("IB ist not subscribed, security cannot be unsubscribed " + security);
        }

        // get the tickerId by querying the TickWindow
        Integer tickerId = getTickDao().findTickerIdBySecurity(security.getId());
        if (tickerId == null) {
            throw new IBMarketDataServiceException("tickerId for security " + security + " was not found");
        }

        client.cancelMktData(tickerId);

        UnsubscribeTickVO unsubscribeTickEvent = new UnsubscribeTickVO();
        unsubscribeTickEvent.setSecurityId(security.getId());
        EsperManager.sendEvent(StrategyImpl.BASE, unsubscribeTickEvent);

        logger.debug("cancelled market data for : " + security);
    }

    @Override
    public void destroy() throws Exception {

        if (client != null && client.isConnected()) {
            client.disconnect();
        }
    }
}
