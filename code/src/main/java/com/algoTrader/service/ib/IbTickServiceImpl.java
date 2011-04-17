package com.algoTrader.service.ib;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.DisposableBean;

import com.algoTrader.entity.Forex;
import com.algoTrader.entity.Future;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.DateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.RawTickVO;
import com.ib.client.Contract;
import com.ib.client.TickType;

public class IbTickServiceImpl extends IbTickServiceBase implements DisposableBean {

    private static Logger logger = MyLogger.getLogger(IbTickServiceImpl.class.getName());

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static boolean ibEnabled = "IB".equals(ConfigurationUtil.getBaseConfig().getString("marketChannel"));
    private static boolean tickServiceEnabled = ConfigurationUtil.getBaseConfig().getBoolean("ib.tickServiceEnabled");

    private static String genericTickList = ConfigurationUtil.getBaseConfig().getString("ib.genericTickList");

    private DefaultClientSocket client;
    private DefaultWrapper wrapper;

    private Map<Integer, Tick> requestIdToTickMap;
    private Map<Security, Integer> securityToRequestIdMap;
    private Set<Security> validSecurities;

    private static int clientId = 1;

    protected void handleInit() throws InterruptedException {

        if (!ibEnabled || simulation || !tickServiceEnabled)
            return;

        this.wrapper = new DefaultWrapper(clientId) {

            public void tickPrice(int requestId, int field, double price, int canAutoExecute) {

                Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(requestId);

                if (tick == null)
                    return;

                // for indexes we get -1 if there is no data available
                if (price == -1) {
                    price = 0;
                }

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
            }

            public void tickSize(int requestId, int field, int size) {

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
            }

            public void tickString(int requestId, int field, String value) {

                Tick tick = IbTickServiceImpl.this.requestIdToTickMap.get(requestId);

                if (tick == null)
                    return;

                if (field == TickType.LAST_TIMESTAMP) {
                    tick.setLastDateTime(new Date(Long.parseLong(value + "000")));
                }
                checkValidity(tick);
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

                Security security = tick.getSecurity();
                if (isValid(tick)) {
                    IbTickServiceImpl.this.validSecurities.add(security);
                } else {
                    IbTickServiceImpl.this.validSecurities.remove(tick.getSecurity());

                }
            }

            private boolean isValid(Tick tick) {

                if (tick.getSecurity() instanceof StockOption) {

                    // stockOptions need to have a bis/ask volume / openIntrest
                    // but might not have a last/lastDateTime yet on the current day
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
                    if (tick.getSettlement() == null)
                        return false;

                } else if (tick.getSecurity() instanceof Future) {

                    // futures need to have a bis/ask volume
                    // but might not have a last/lastDateTime yet on the current day
                    if (tick.getVolBid() == 0)
                        return false;
                    if (tick.getVolAsk() == 0)
                        return false;
                    if (tick.getBid() != null && tick.getBid().doubleValue() <= 0)
                        return false;
                    if (tick.getAsk() != null && tick.getAsk().doubleValue() <= 0)
                        return false;
                    if (tick.getSettlement() == null)
                        return false;

                } else if (tick.getSecurity() instanceof Forex) {

                    // no special checks
                    if (tick.getVolBid() == 0)
                        return false;
                    if (tick.getVolAsk() == 0)
                        return false;

                } else {


                    if (tick.getLast() == null)
                        return false;
                    if (tick.getLastDateTime() == null)
                        return false;
                    if (tick.getSettlement() == null)
                        return false;
                }

                // check these fields for all security-types
                if (tick.getBid() == null)
                    return false;
                if (tick.getAsk() == null)
                    return false;

                return true;
            }
        };

        this.client = new DefaultClientSocket(this.wrapper);

        connect();
    }

    protected void handleConnect() {

        if (!ibEnabled || simulation || !tickServiceEnabled)
            return;

        this.requestIdToTickMap = new ConcurrentHashMap<Integer, Tick>();
        this.securityToRequestIdMap = new ConcurrentHashMap<Security, Integer>();
        this.validSecurities = new CopyOnWriteArraySet<Security>();

        this.client.connect(clientId);

        // if no market data farm is connected we have to force requestMarketData now
        if (this.wrapper.getState().equals(ConnectionState.CONNECTED)) {
            requestMarketData();
        }
    }

    protected ConnectionState handleGetConnectionState() {

        if (this.wrapper == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.wrapper.getState();
        }
    }

    private void requestMarketData() {

        if ((this.wrapper.getState().equals(ConnectionState.CONNECTED) || this.wrapper.getState().equals(ConnectionState.READY)) && !this.wrapper.isRequested()) {

            this.wrapper.setRequested(true);

            List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
            for (Security security : securities) {

                int requestId = RequestIdManager.getInstance().getNextRequestId();

                Contract contract = IbUtil.getContract(security);
                this.client.reqMktData(requestId, contract, genericTickList, false);

                Tick tick = new TickImpl();
                tick.setSecurity(security);

                this.requestIdToTickMap.put(requestId, tick);
                this.securityToRequestIdMap.put(security, requestId);
                this.validSecurities.remove(tick.getSecurity());

                logger.debug("requested market data for : " + security.getSymbol());
            }

            if (this.wrapper.getState().equals(ConnectionState.READY)) {
                this.wrapper.setState(ConnectionState.SUBSCRIBED);
                logger.debug("connectionState: " + this.wrapper.getState());
            }

        }
    }

    protected RawTickVO handleRetrieveTick(Security security) throws Exception {

        // security might have been removed from the watchlist
        if (!this.securityToRequestIdMap.containsKey(security)) {
            return null;
        }

        if (!this.wrapper.getState().equals(ConnectionState.SUBSCRIBED) || !this.validSecurities.contains(security)) {
            return null;
        }

        Integer requestId = this.securityToRequestIdMap.get(security);
        Tick tick = this.requestIdToTickMap.get(requestId);

        if (tick != null) {
            tick.setDateTime(DateUtil.getCurrentEPTime());
            return getTickDao().toRawTickVO(tick);
        } else {
            // might not be on watchlist any more
            return null;
        }
    }

    protected void handlePutOnExternalWatchlist(Security security) throws Exception {

        if (!simulation) {

            if (!this.wrapper.getState().equals(ConnectionState.SUBSCRIBED)) {
                throw new IbTickServiceException("TWS ist not subscribed, security cannot be put on watchlist " + security.getSymbol());
            }

            int requestId = RequestIdManager.getInstance().getNextRequestId();

            Contract contract = IbUtil.getContract(security);
            this.client.reqMktData(requestId, contract, genericTickList, false);

            Tick tick = new TickImpl();
            tick.setSecurity(security);

            this.requestIdToTickMap.put(requestId, tick);
            this.securityToRequestIdMap.put(security, requestId);
            this.validSecurities.remove(tick.getSecurity());
        }
    }

    protected void handleRemoveFromExternalWatchlist(Security security) throws Exception {

        if (!simulation) {

            if (!this.wrapper.getState().equals(ConnectionState.SUBSCRIBED)) {
                throw new IbTickServiceException("TWS ist not subscribed, security cannot be removed from watchlist " + security.getSymbol());
            }

            Integer requestId = this.securityToRequestIdMap.get(security);

            if (requestId != null) {
                this.client.cancelMktData(requestId);

                this.requestIdToTickMap.remove(requestId);
                this.securityToRequestIdMap.remove(security);
                this.validSecurities.remove(security);
            }
        }
    }

    public void destroy() throws Exception {

        if (this.client != null) {
            this.client.disconnect();
        }
    }
}
