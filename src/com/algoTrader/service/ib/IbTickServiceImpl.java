package com.algoTrader.service.ib;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.entity.TickImpl;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.PropertiesUtil;
import com.algoTrader.util.RoundUtil;
import com.ib.client.AnyWrapper;
import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.TickType;

public class IbTickServiceImpl extends IbTickServiceBase {

    private static Logger logger = MyLogger.getLogger(IbTickServiceImpl.class.getName());

    private static boolean ibEnabled = "IB".equals(PropertiesUtil.getProperty("marketChannel"));
    private static int port = PropertiesUtil.getIntProperty("ib.port");

    private EClientSocket client;
    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private Map<Integer, Tick> tickMap = new HashMap<Integer, Tick>();
    private Map<Integer, Boolean> returnedMap = new HashMap<Integer, Boolean>();

    private static int clientId = 1;

    public IbTickServiceImpl() {

        if (!ibEnabled)
            return;

        AnyWrapper wrapper = new DefaultWrapper() {

            public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.tickMap.get(tickerId);

                    if (field == TickType.ASK) {
                        tick.setAsk(RoundUtil.getBigDecimal(price));
                    } else if (field == TickType.BID) {
                        tick.setBid(RoundUtil.getBigDecimal(price));
                    } else if (field == TickType.LAST) {
                        tick.setLast(RoundUtil.getBigDecimal(price));
                    }
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void tickSize(int tickerId, int field, int size) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.tickMap.get(tickerId);

                    if (field == TickType.ASK_SIZE) {
                        tick.setVolAsk(size);
                    } else if (field == TickType.BID_SIZE) {
                        tick.setVolBid(size);
                    } else if (field == TickType.VOLUME) {
                        tick.setVol(size);
                    } else if (field == TickType.OPEN_INTEREST) {
                        tick.setOpenIntrest(size);
                    }
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void tickString(int tickerId, int field, String value) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.tickMap.get(tickerId);

                    if (field == TickType.LAST_TIMESTAMP) {
                        tick.setLastDateTime(new Date(Long.parseLong(value + "000")));
                    }
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void tickSnapshotEnd(int tickerId) {

                IbTickServiceImpl.this.lock.lock();
                try {
                    Tick tick = IbTickServiceImpl.this.tickMap.get(tickerId);

                    tick.setDateTime(new Date());

                    IbTickServiceImpl.this.returnedMap.put(tickerId, true);
                    IbTickServiceImpl.this.condition.signal();
                } finally {
                    IbTickServiceImpl.this.lock.unlock();
                }
            }

            public void error(int tickerId, int errorCode, String errorMsg) {

                if (errorCode < 1000) {

                    logger.error("id: " + tickerId + " errorCode: " + errorCode + " " + errorMsg);

                    IbTickServiceImpl.this.lock.lock();
                    try {
                        IbTickServiceImpl.this.tickMap.put(tickerId, null);
                        IbTickServiceImpl.this.returnedMap.put(tickerId, true);
                        IbTickServiceImpl.this.condition.signal();
                    } finally {
                        IbTickServiceImpl.this.lock.unlock();
                    }
                } else {
                    super.error(tickerId, errorCode, errorMsg);
                }
            }
        };

        this.client = new EClientSocket(wrapper);
        this.client.eConnect(null, port, clientId);
    }

    protected Tick handleRetrieveTick(Security security) throws Exception {

        int tickerId = RequestIdManager.getInstance().getNextRequestId();

        Tick tick = new TickImpl();
        this.tickMap.put(tickerId, tick);
        this.returnedMap.put(tickerId, false);

        Contract contract = IbUtil.getContract(security);

        this.lock.lock();

        try {

            this.client.reqMktData(tickerId, contract, null, true);
            while (!this.returnedMap.get(tickerId)) {
                this.condition.await();
            }

            tick = this.tickMap.get(tickerId);

            this.returnedMap.remove(tickerId);
            this.tickMap.remove(tickerId);

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.lock.unlock();
        }
        return tick;
    }
}
