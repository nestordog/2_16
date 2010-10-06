package com.algoTrader.service.ib;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.ib.client.Contract;
import com.ib.client.TickType;

public class IbTickServiceImpl extends IbTickServiceBase implements InitializingBean {

    private static Logger logger = MyLogger.getLogger(IbTickServiceBase.class.getName());

    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");
    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));

    private static int retrievalTimeout = PropertiesUtil.getIntProperty("ib.retrievalTimeout");
    private static String genericTickList = PropertiesUtil.getProperty("ib.genericTickList");

    private DefaultClientSocket client;
    private DefaultWrapper wrapper;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<Integer, Tick> requestIdToTickMap;
    private Map<Security, Integer> securityToRequestIdMap;
    private Set<Security> validSecurities;

    private static int clientId = 1;

    public void afterPropertiesSet() throws Exception {

        init();
    }

    protected void handleInit() throws InterruptedException {

        if (!ibEnabled || simulation)
            return;

        this.wrapper = new DefaultWrapper() {

            public void tickPrice(int requestId, int field, double price, int canAutoExecute) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(requestId);

                    if (tick == null)
                        return;

                    if (field == TickType.BID) {
                        tick.setBid(RoundUtil.getBigDecimal(price));
                    } else if (field == TickType.ASK) {
                        tick.setAsk(RoundUtil.getBigDecimal(price));
                    } else if (field == TickType.LAST) {
                        tick.setLast(RoundUtil.getBigDecimal(price));
                    } else if (field == TickType.CLOSE) {
                        tick.setSettlement(RoundUtil.getBigDecimal(price));
                    }
                    checkValidity(tick);
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void tickSize(int requestId, int field, int size) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(requestId);

                    if (tick == null)
                        return;

                    if (field == TickType.ASK_SIZE) {
                        tick.setVolAsk(size);
                    } else if (field == TickType.BID_SIZE) {
                        tick.setVolBid(size);
                    } else if (field == TickType.VOLUME) {
                        tick.setVol(size);
                    }

                    if (tick.getSecurity() instanceof StockOption) {
                        StockOption stockOption = (StockOption) tick.getSecurity();
                        if (field == TickType.OPTION_CALL_OPEN_INTEREST && OptionType.CALL.equals(stockOption.getType())) {
                            tick.setOpenIntrest(size);
                        } else if (field == TickType.OPTION_PUT_OPEN_INTEREST && OptionType.PUT.equals(stockOption.getType())) {
                            tick.setOpenIntrest(size);
                        }
                    }
                    checkValidity(tick);
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void tickString(int requestId, int field, String value) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(requestId);

                    if (tick == null)
                        return;

                    if (field == TickType.LAST_TIMESTAMP) {
                        tick.setLastDateTime(new Date(Long.parseLong(value + "000")));
                    }
                    checkValidity(tick);
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }

            @Override
            public void error(int id, int code, String errorMsg) {

                if (code == 200) {

                    Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(id);
                    logger.debug("No security definition has been found for: " + tick.getSecurity().getSymbol());

                } else {

                    super.error(id, code, errorMsg);

                    // in the following cases we might need to requestMarketData
                    // (again)
                    if (code == 1101 || code == 1102 || code == 2104) {
                        requestMarketData();
                    }
                }
            }

            private void checkValidity(Tick tick) {

                if (isValid(tick)) {
                    IbTickServiceImpl.this.validSecurities.add(tick.getSecurity());
                    IbTickServiceImpl.this.condition.signalAll();
                } else {
                    IbTickServiceImpl.this.validSecurities.remove(tick.getSecurity());

                }
            }

            private boolean isValid(Tick tick) {

                if (tick.getSecurity() instanceof StockOption) {

                    // stockOptions need to have a bis/ask volume / openIntrest
                    if (tick.getVolBid() == 0)
                        return false;
                    if (tick.getVolAsk() == 0)
                        return false;
                    if (tick.getOpenIntrest() == 0)
                        return false;
                    if (tick.getBid() != null && tick.getBid().doubleValue() <= 0)
                        return false;
                    if (tick.getAsk() != null && tick.getAsk().doubleValue() <= 0)
                        return false;
                } else {

                    // stockOptions might not have a last/lastDateTime yet on the current day
                    if (tick.getLast() == null)
                        return false;
                    if (tick.getLastDateTime() == null)
                        return false;
                }

                // check these fields for all security-types
                if (tick.getBid() == null)
                    return false;
                if (tick.getAsk() == null)
                    return false;
                if (tick.getSettlement() == null)
                    return false;

                return true;
            }
        };

        this.client = new DefaultClientSocket(this.wrapper);

        connect();
    }

    protected void handleConnect() {

        this.requestIdToTickMap = new HashMap<Integer, Tick>();
        this.securityToRequestIdMap = new HashMap<Security, Integer>();
        this.validSecurities = new HashSet<Security>();

        this.client.connect(clientId);
    }

    @SuppressWarnings("unchecked")
    private void requestMarketData() {

        if (this.wrapper.getState().equals(ConnectionState.READY) && !this.wrapper.isRequested()) {

            List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
            for (Security security : securities) {

                int requestId = RequestIdManager.getInstance().getNextRequestId();

                Tick tick = new TickImpl();
                tick.setSecurity(security);
                this.requestIdToTickMap.put(requestId, tick);
                this.securityToRequestIdMap.put(security, requestId);

                Contract contract = IbUtil.getContract(security);
                this.client.reqMktData(requestId, contract, genericTickList, false);

                logger.debug("requested market data for : " + security.getSymbol());
            }
            this.wrapper.setState(ConnectionState.SUBSCRIBED);
            this.wrapper.setRequested(true);
            logger.debug("connectionState: " + this.wrapper.getState());
        }
    }

    protected Tick handleRetrieveTick(Security security) throws Exception {

        this.lock.lock();

        Tick tick;
        try {

            while (!this.wrapper.getState().equals(ConnectionState.SUBSCRIBED) || !this.validSecurities.contains(security)) {
                if (!this.condition.await(retrievalTimeout, TimeUnit.MILLISECONDS)) {
                    // could not retrieve tick in time for security
                    return null;
                }
            }

            Integer requestId = this.securityToRequestIdMap.get(security);
            Tick tempTick = this.requestIdToTickMap.get(requestId);

            if (tempTick == null) {
                // might not be on watchlist any more
                return null;
            }

            tick = (Tick) BeanUtils.cloneBean(tempTick);
            tick.setDateTime(DateUtil.getCurrentEPTime());
            tick.setSecurity(security);

        } finally {
            this.lock.unlock();
        }
        return tick;
    }

    protected void handlePutOnExternalWatchlist(StockOption stockOption) throws Exception {

        if (!simulation) {

            while (!this.wrapper.getState().equals(ConnectionState.SUBSCRIBED)) {
                if (!this.condition.await(retrievalTimeout, TimeUnit.MILLISECONDS)) {
                    throw new IbTickServiceException("TWS ist not subscribed, stockOption cannot be put on watchlist " + stockOption.getSymbol());
                }
            }

            int requestId = RequestIdManager.getInstance().getNextRequestId();

            Tick tick = new TickImpl();
            tick.setSecurity(stockOption);
            this.requestIdToTickMap.put(requestId, tick);
            this.securityToRequestIdMap.put(stockOption, requestId);

            Contract contract = IbUtil.getContract(stockOption);
            this.client.reqMktData(requestId, contract, genericTickList, false);
        }
    }

    protected void handleRemoveFromExternalWatchlist(StockOption stockOption) throws Exception {

        if (!simulation) {

            while (!this.wrapper.getState().equals(ConnectionState.SUBSCRIBED)) {
                if (!this.condition.await(retrievalTimeout, TimeUnit.MILLISECONDS)) {
                    throw new IbTickServiceException("TWS ist not subscribed, stockOption cannot be removed from watchlist " + stockOption.getSymbol());
                }
            }

            Integer requestId = this.securityToRequestIdMap.get(stockOption);

            if (requestId != null) {
                this.client.cancelMktData(requestId);

                this.requestIdToTickMap.remove(requestId);
                this.securityToRequestIdMap.remove(stockOption);
            }
        }
    }
}
