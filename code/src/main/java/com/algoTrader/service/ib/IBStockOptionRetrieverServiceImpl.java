package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.StockOption;
import com.algoTrader.entity.security.StockOptionFamily;
import com.algoTrader.entity.security.StockOptionImpl;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.Market;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.stockOption.StockOptionSymbol;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.RoundUtil;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;

public class IBStockOptionRetrieverServiceImpl extends IBStockOptionRetrieverServiceBase {

    private static final long serialVersionUID = 6446509772400405052L;

    private static Logger logger = MyLogger.getLogger(IBStockOptionRetrieverServiceImpl.class.getName());
    private static SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");

    private static boolean simulation = ConfigurationUtil.getBaseConfig().getBoolean("simulation");
    private static boolean ibEnabled = "IB".equals(ConfigurationUtil.getBaseConfig().getString("marketChannel"));
    private static boolean stockOptionRetrieverServiceEnabled = ConfigurationUtil.getBaseConfig().getBoolean("ib.stockOptionRetrieverServiceEnabled");

    private IBClient client;
    private IBDefaultAdapter wrapper;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private List<ContractDetails> contractDetailsList;

    private static int clientId = 3;

    @Override
    protected void handleRetrieveAllStockOptionsForUnderlaying(int underlayingId) throws Exception {

        Security underlaying = getSecurityDao().load(underlayingId);
        StockOptionFamily family = getStockOptionFamilyDao().findByUnderlaying(underlaying.getId());

        this.contractDetailsList = new ArrayList<ContractDetails>();

        IBStockOptionRetrieverServiceImpl.this.lock.lock();

        try {

            int requestId = RequestIDGenerator.singleton().getNextRequestId();
            Contract contract = IBUtil.getContract(underlaying);
            contract.m_secType = "OPT";

            this.client.reqContractDetails(requestId, contract);

            this.condition.await();

        } finally {
            IBStockOptionRetrieverServiceImpl.this.lock.unlock();
        }

        // comparator based on isin
        Comparator<StockOption> comparator = new Comparator<StockOption>() {
            @Override
            public int compare(StockOption o1, StockOption o2) {
                return o1.getIsin().compareTo(o2.getIsin());
            }
        };

        // get all current stockOptions (sorted by isin)
        Set<StockOption> existingStockOptions = new TreeSet<StockOption>(comparator);
        existingStockOptions.addAll(getStockOptionDao().loadAll());

        Set<StockOption> newStockOptions = new TreeSet<StockOption>(comparator);
        for (ContractDetails contractDetails : this.contractDetailsList) {

            StockOption stockOption = new StockOptionImpl();
            stockOption.setSecurityFamily(family);

            Contract contract = contractDetails.m_summary;
            OptionType type = "C".equals(contract.m_right) ? OptionType.CALL : OptionType.PUT;
            BigDecimal strike = RoundUtil.getBigDecimal(contract.m_strike, family.getScale());
            Date expiration = dayFormat.parse(contract.m_expiry);

            if (underlaying.getSecurityFamily().getMarket().equals(Market.CBOE) || underlaying.getSecurityFamily().getMarket().equals(Market.SOFFEX)) {
                expiration = DateUtils.addDays(expiration, 1);
            }

            final String isin = StockOptionSymbol.getIsin(family, expiration, type, strike);
            String symbol = StockOptionSymbol.getSymbol(family, expiration, type, strike);


            stockOption.setIsin(isin);
            stockOption.setSymbol(symbol);
            stockOption.setType(type);
            stockOption.setStrike(strike);
            stockOption.setExpiration(expiration);
            stockOption.setUnderlaying(underlaying);
            stockOption.setSecurityFamily(family);

            // ignore stockOptions that already exist
            if (!existingStockOptions.contains(stockOption)) {
                newStockOptions.add(stockOption);
            }
        }

        getStockOptionDao().create(newStockOptions);

        logger.debug("retrieved options: " + newStockOptions);
    }

    @Override
    protected void handleRetrieveAllStockOptions() throws Exception {
        throw new UnsupportedOperationException("handleRetrieveAllStockOptions is not implemented yet");
    }

    @Override
    protected StockOption handleRetrieveStockOption(int underlayingId, Date expiration, BigDecimal strike, OptionType type) throws Exception {
        throw new UnsupportedOperationException("handleRetrieveStockOption is not implemented yet");
    }

    @Override
    protected void handleInit() throws java.lang.Exception {

        if (!ibEnabled || simulation || !stockOptionRetrieverServiceEnabled) {
            return;
        }

        this.wrapper = new IBDefaultAdapter(clientId) {

            @Override
            public void contractDetails(int reqId, ContractDetails contractDetails) {

                IBStockOptionRetrieverServiceImpl.this.contractDetailsList.add(contractDetails);
            }

            @Override
            public void contractDetailsEnd(int reqId) {

                IBStockOptionRetrieverServiceImpl.this.lock.lock();

                try {
                    IBStockOptionRetrieverServiceImpl.this.condition.signalAll();
                } finally {
                    IBStockOptionRetrieverServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void connectionClosed() {

                super.connectionClosed();

                connect();
            }
        };

        this.client = new IBClient(clientId, this.wrapper);

        connect();
    }

    @Override
    protected void handleConnect() {

        if (!ibEnabled || simulation || !stockOptionRetrieverServiceEnabled) {
            return;
        }

        this.client.connect();
    }

    @Override
    protected ConnectionState handleGetConnectionState() {

        if (this.wrapper == null) {
            return ConnectionState.DISCONNECTED;
        } else {
            return this.wrapper.getState();
        }
    }
}
