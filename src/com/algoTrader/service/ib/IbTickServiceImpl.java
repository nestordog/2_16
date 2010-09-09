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
import org.springframework.beans.factory.InitializingBean;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.ib.client.AnyWrapper;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.TickType;

public class IbTickServiceImpl extends IbTickServiceBase implements InitializingBean {

    private static boolean simulation = PropertiesUtil.getBooleanProperty("simulation");
    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));

    private static int port = PropertiesUtil.getIntProperty("ib.port");
    private static int retrievalTimeout = PropertiesUtil.getIntProperty("ib.retrievalTimeout");
    private static String genericTickList = PropertiesUtil.getProperty("ib.genericTickList");

    private EClientSocket client;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<Integer, Tick> requestIdToTickMap = new HashMap<Integer, Tick>();
    private Map<Security, Integer> securityToRequestIdMap = new HashMap<Security, Integer>();
    private Set<Security> validSecurities = new HashSet<Security>();

    private static int clientId = 1;

    public void afterPropertiesSet() throws Exception {

        init();
    }

    @SuppressWarnings("unchecked")
    protected void handleInit() {

        if (!ibEnabled)
            return;

        AnyWrapper wrapper = new DefaultWrapper() {

            public void tickPrice(int requestId, int field, double price, int canAutoExecute) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(requestId);

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

                    if (field == TickType.LAST_TIMESTAMP) {
                        tick.setLastDateTime(new Date(Long.parseLong(value + "000")));
                    }
                    checkValidity(tick);
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            private void checkValidity(Tick tick) {

                if (tick.getSecurity() instanceof StockOption) {
                    if (tick.getBid() == null)
                        return;
                    if (tick.getAsk() == null)
                        return;
                    if (tick.getVolBid() == 0)
                        return;
                    if (tick.getVolAsk() == 0)
                        return;
                    if (tick.getOpenIntrest() == 0)
                        return;
                } else {

                    // stockOptions might not have a last/lastDateTime yet on the current day
                    if (tick.getLast() == null)
                        return;
                    if (tick.getLastDateTime() == null)
                        return;
                }

                IbTickServiceImpl.this.validSecurities.add(tick.getSecurity());
                IbTickServiceImpl.this.condition.signalAll();
            }
        };

        this.client = new EClientSocket(wrapper);
        this.client.eConnect(null, port, clientId);

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            int requestId = RequestIdManager.getInstance().getNextRequestId();

            Tick tick = new TickImpl();
            tick.setSecurity(security);
            this.requestIdToTickMap.put(requestId, tick);
            this.securityToRequestIdMap.put(security, requestId);

            Contract contract = IbUtil.getContract(security);
            this.client.reqMktData(requestId, contract, genericTickList, false);
        }
    }

    protected Tick handleRetrieveTick(Security security) throws Exception {

        this.lock.lock();

        Tick tick;
        try {

            while (!this.validSecurities.contains(security)) {
                if (!this.condition.await(retrievalTimeout, TimeUnit.SECONDS)) {

                    throw new IbTickServiceException("could not retrieve tick in time for security: " + security);
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

        } finally {
            this.lock.unlock();
        }
        return tick;
    }

    protected void handlePutOnExternalWatchlist(StockOption stockOption) throws Exception {

        if (!simulation) {
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
            Integer requestId = this.securityToRequestIdMap.get(stockOption);

            if (requestId != null) {
                this.client.cancelMktData(requestId);

                this.requestIdToTickMap.remove(requestId);
                this.securityToRequestIdMap.remove(stockOption);
            }
        }
    }
}
