/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.ib;

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

import ch.algotrader.adapter.ib.IBClient;
import ch.algotrader.adapter.ib.IBDefaultMessageHandler;
import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBMarketConverter;
import ch.algotrader.entity.security.ForexFutureImpl;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.StockOptionImpl;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.stockOption.StockOptionSymbol;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

import ch.algotrader.entity.security.ForexFuture;
import ch.algotrader.entity.security.ForexFutureFamily;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.StockOption;
import ch.algotrader.entity.security.StockOptionFamily;
import ch.algotrader.enumeration.Market;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.service.ib.IBNativeSecurityRetrieverServiceBase;
import com.ib.client.Contract;
import com.ib.client.ContractDetails;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeSecurityRetrieverServiceImpl extends IBNativeSecurityRetrieverServiceBase {

    private static final long serialVersionUID = 6446509772400405052L;

    private static Logger logger = MyLogger.getLogger(IBNativeSecurityRetrieverServiceImpl.class.getName());
    private static SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss");

    private IBClient client;
    private IBDefaultMessageHandler messageHandler;

    private Lock lock = new ReentrantLock();
    private Condition condition = this.lock.newCondition();

    private List<ContractDetails> contractDetailsList;

    private static int clientId = 3;

    @Override
    protected void handleRetrieve(int securityFamilyId) throws Exception {

        SecurityFamily securityFamily = getSecurityFamilyDao().get(securityFamilyId);

        // engage the retrieval process
        retrieveContractDetails(securityFamily);

        if (securityFamily instanceof StockOptionFamily) {
            retrieveStockOptions((StockOptionFamily) securityFamily);
        } else if (securityFamily instanceof ForexFutureFamily) {
            retrieveForexFutures((ForexFutureFamily) securityFamily);
        } else if (securityFamily instanceof FutureFamily) {
            retrieveFutures((FutureFamily) securityFamily);
        } else {
            throw new IllegalArgumentException(securityFamily.getClass() + " not allowed");
        }
    }

    private void retrieveStockOptions(StockOptionFamily family) throws Exception {

        Security underlying = family.getUnderlying();

        // get all current stockOptions (sorted by isin)
        Set<Security> existingStockOptions = new TreeSet<Security>(getComparator());
        existingStockOptions.addAll(getStockOptionDao().findStockOptionsBySecurityFamily(family.getId()));

        Set<StockOption> newStockOptions = new TreeSet<StockOption>();
        for (ContractDetails contractDetails : this.contractDetailsList) {

            StockOption stockOption = new StockOptionImpl();
            stockOption.setSecurityFamily(family);

            Contract contract = contractDetails.m_summary;
            OptionType type = "C".equals(contract.m_right) ? OptionType.CALL : OptionType.PUT;
            BigDecimal strike = RoundUtil.getBigDecimal(contract.m_strike, family.getScale());
            Date expiration = format.parse(contract.m_expiry + "130000");

            if (underlying.getSecurityFamily().getMarket().equals(Market.CBOE) || underlying.getSecurityFamily().getMarket().equals(Market.SOFFEX)) {
                expiration = DateUtils.addDays(expiration, 1);
            }

            final String isin = StockOptionSymbol.getIsin(family, expiration, type, strike);
            String symbol = StockOptionSymbol.getSymbol(family, expiration, type, strike, false);
            String ric = StockOptionSymbol.getRic(family, expiration, type, strike);
            String conid = String.valueOf(contract.m_conId);

            stockOption.setSymbol(symbol);
            stockOption.setIsin(isin);
            stockOption.setRic(ric);
            stockOption.setConid(conid);
            stockOption.setType(type);
            stockOption.setStrike(strike);
            stockOption.setExpiration(expiration);
            stockOption.setUnderlying(underlying);
            stockOption.setSecurityFamily(family);

            // ignore stockOptions that already exist
            if (!existingStockOptions.contains(stockOption)) {
                newStockOptions.add(stockOption);
            }
        }

        getStockOptionDao().create(newStockOptions);

        logger.debug("retrieved options for optionfamily: " + family.getName() + " " + newStockOptions);
    }

    private void retrieveFutures(FutureFamily family) throws Exception {

        Security underlying = family.getUnderlying();

        // get all current futures (sorted by isin)
        Set<Future> existingFutures = new TreeSet<Future>(getComparator());
        existingFutures.addAll(getFutureDao().findFuturesBySecurityFamily(family.getId()));

        Set<Future> newFutures = new TreeSet<Future>();
        for (ContractDetails contractDetails : this.contractDetailsList) {

            Future future = new FutureImpl();
            future.setSecurityFamily(family);

            Contract contract = contractDetails.m_summary;
            Date expiration = format.parse(contract.m_expiry + "130000");

            if (underlying.getSecurityFamily().getMarket().equals(Market.CBOE) || underlying.getSecurityFamily().getMarket().equals(Market.SOFFEX)) {
                expiration = DateUtils.addDays(expiration, 1);
            }

            String symbol = FutureSymbol.getSymbol(family, expiration);
            final String isin = FutureSymbol.getIsin(family, expiration);
            String ric = FutureSymbol.getRic(family, expiration);
            String conid = String.valueOf(contract.m_conId);

            future.setSymbol(symbol);
            future.setIsin(isin);
            future.setRic(ric);
            future.setConid(conid);
            future.setExpiration(expiration);
            future.setUnderlying(underlying);
            future.setSecurityFamily(family);

            // ignore futures that already exist
            if (!existingFutures.contains(future)) {
                newFutures.add(future);
            }
        }

        getFutureDao().create(newFutures);

        logger.debug("retrieved futures for futurefamily: " + family.getName() + " " + newFutures);
    }

    private void retrieveForexFutures(ForexFutureFamily family) throws Exception {

        Security underlying = family.getUnderlying();

        // get all current forexFutures (sorted by isin)
        Set<ForexFuture> existingForexFutures = new TreeSet<ForexFuture>(getComparator());
        existingForexFutures.addAll(getForexFutureDao().findForexFuturesBySecurityFamily(family.getId()));

        Set<ForexFuture> newForexFutures = new TreeSet<ForexFuture>();
        for (ContractDetails contractDetails : this.contractDetailsList) {

            ForexFuture forexFuture = new ForexFutureImpl();
            forexFuture.setSecurityFamily(family);

            Contract contract = contractDetails.m_summary;
            Date expiration = format.parse(contract.m_expiry + "130000");

            if (underlying.getSecurityFamily().getMarket().equals(Market.CBOE) || underlying.getSecurityFamily().getMarket().equals(Market.SOFFEX)) {
                expiration = DateUtils.addDays(expiration, 1);
            }

            String symbol = FutureSymbol.getSymbol(family, expiration);
            final String isin = FutureSymbol.getIsin(family, expiration);
            String ric = FutureSymbol.getRic(family, expiration);
            String conid = String.valueOf(contract.m_conId);

            forexFuture.setSymbol(symbol);
            forexFuture.setIsin(isin);
            forexFuture.setRic(ric);
            forexFuture.setConid(conid);
            forexFuture.setExpiration(expiration);
            forexFuture.setUnderlying(underlying);
            forexFuture.setSecurityFamily(family);
            forexFuture.setBaseCurrency(family.getBaseCurrency());

            // ignore forexFutures that already exist
            if (!existingForexFutures.contains(forexFuture)) {
                newForexFutures.add(forexFuture);
            }
        }

        getForexFutureDao().create(newForexFutures);

        logger.debug("retrieved forexFutures for forexFuturefamily: " + family.getName() + " " + newForexFutures);
    }

    private void retrieveContractDetails(SecurityFamily securityFamily) throws InterruptedException {

        this.contractDetailsList = new ArrayList<ContractDetails>();

        IBNativeSecurityRetrieverServiceImpl.this.lock.lock();

        try {

            int requestId = IBIdGenerator.getInstance().getNextRequestId();
            Contract contract = new Contract();
            contract.m_currency = securityFamily.getCurrency().toString();
            contract.m_symbol = securityFamily.getBaseSymbol();
            contract.m_exchange = IBMarketConverter.marketToString(securityFamily.getMarket());

            if (securityFamily instanceof StockOptionFamily) {
                contract.m_secType = "OPT";
            } else if (securityFamily instanceof FutureFamily) {
                contract.m_secType = "FUT";
            }

            this.client.reqContractDetails(requestId, contract);

            this.condition.await();

        } finally {
            IBNativeSecurityRetrieverServiceImpl.this.lock.unlock();
        }
    }

    private Comparator<Security> getComparator() {

        // comparator based on isin
        Comparator<Security> comparator = new Comparator<Security>() {
            @Override
            public int compare(Security o1, Security o2) {
                return o1.getIsin().compareTo(o2.getIsin());
            }
        };
        return comparator;
    }

    @Override
    protected void handleInit() throws java.lang.Exception {

        this.messageHandler = new IBDefaultMessageHandler(clientId) {

            @Override
            public void contractDetails(int reqId, ContractDetails contractDetails) {

                IBNativeSecurityRetrieverServiceImpl.this.contractDetailsList.add(contractDetails);
            }

            @Override
            public void contractDetailsEnd(int reqId) {

                IBNativeSecurityRetrieverServiceImpl.this.lock.lock();

                try {
                    IBNativeSecurityRetrieverServiceImpl.this.condition.signalAll();
                } finally {
                    IBNativeSecurityRetrieverServiceImpl.this.lock.unlock();
                }
            }

            @Override
            public void connectionClosed() {

                super.connectionClosed();

                IBNativeSecurityRetrieverServiceImpl.this.client.connect();
            }
        };

        this.client = getIBClientFactory().getClient(clientId, this.messageHandler);

        this.client.connect();
    }
}
