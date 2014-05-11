/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.ib;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;

import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeSecurityRetrieverServiceImpl extends IBNativeSecurityRetrieverServiceBase {

    private static final Logger logger = MyLogger.getLogger(IBNativeSecurityRetrieverServiceImpl.class.getName());
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");

    private BlockingQueue<ContractDetails> contractDetailsQueue;

    public void setContractDetailsQueue(BlockingQueue<ContractDetails> contractDetailsQueue) {
        this.contractDetailsQueue = contractDetailsQueue;
    }

    @Override
    protected void handleRetrieve(int securityFamilyId) throws Exception {

        SecurityFamily securityFamily = getSecurityFamilyDao().get(securityFamilyId);

        int requestId = getIBIdGenerator().getNextRequestId();
        Contract contract = new Contract();

        contract.m_symbol = securityFamily.getBaseSymbol(Broker.IB);

        contract.m_currency = securityFamily.getCurrency().toString();

        contract.m_exchange = securityFamily.getMarket(Broker.IB);

        contract.m_multiplier = String.valueOf(securityFamily.getContractSize());

        if (securityFamily instanceof OptionFamily) {
            contract.m_secType = "OPT";
        } else if (securityFamily instanceof FutureFamily) {
            contract.m_secType = "FUT";
        } else {
            throw new IllegalArgumentException("illegal securityFamily type");
        }

        if (securityFamily.getTradingClass() != null) {
            contract.m_tradingClass = securityFamily.getTradingClass();
        }

        // send request
        getIBSession().reqContractDetails(requestId, contract);

        Set<ContractDetails> contractDetailsSet = retrieveContractDetails();

        if (securityFamily instanceof OptionFamily) {
            retrieveOptions((OptionFamily) securityFamily, contractDetailsSet);
        } else if (securityFamily instanceof FutureFamily) {
            retrieveFutures((FutureFamily) securityFamily, contractDetailsSet);
        } else {
            throw new IllegalArgumentException("illegal securityFamily type");
        }
    }

    @Override
    protected void handleRetrieveStocks(int securityFamilyId, String symbol) throws Exception {

        SecurityFamily securityFamily = getSecurityFamilyDao().get(securityFamilyId);

        int requestId = getIBIdGenerator().getNextRequestId();
        Contract contract = new Contract();

        contract.m_symbol = symbol;

        contract.m_currency = securityFamily.getCurrency().toString();

        contract.m_exchange = securityFamily.getMarket(Broker.IB);

        contract.m_secType = "STK";

        logger.debug("retrieving stock " + symbol);

        getIBSession().reqContractDetails(requestId, contract);

        Set<ContractDetails> contractDetailsSet = retrieveContractDetails();

        retrieveStocks(securityFamily, contractDetailsSet);
    }

    private Set<ContractDetails> retrieveContractDetails() throws InterruptedException {

        Set<ContractDetails> contractDetailsSet = new HashSet<ContractDetails>();

        ContractDetails contractDetails;
        while (!((contractDetails = this.contractDetailsQueue.take()).m_summary.m_symbol == null)) {
            contractDetailsSet.add(contractDetails);
        }

        return contractDetailsSet;
    }

    private void retrieveOptions(OptionFamily securityFamily, Set<ContractDetails> contractDetailsSet) throws Exception {

        // get all current options
        Set<Security> existingOptions = new TreeSet<Security>(getComparator());
        existingOptions.addAll(getOptionDao().findBySecurityFamily(securityFamily.getId()));

        Set<Option> newOptions = new TreeSet<Option>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Option option = Option.Factory.newInstance();

            Contract contract = contractDetails.m_summary;
            OptionType type = "C".equals(contract.m_right) ? OptionType.CALL : OptionType.PUT;
            BigDecimal strike = RoundUtil.getBigDecimal(contract.m_strike, securityFamily.getScale());
            Date expiration = format.parse(contract.m_expiry);

            final String isin = OptionSymbol.getIsin(securityFamily, expiration, type, strike);
            String symbol = OptionSymbol.getSymbol(securityFamily, expiration, type, strike, false);
            String ric = OptionSymbol.getRic(securityFamily, expiration, type, strike);
            String conid = String.valueOf(contract.m_conId);

            option.setSymbol(symbol);
            option.setIsin(isin);
            option.setRic(ric);
            option.setConid(conid);
            option.setType(type);
            option.setStrike(strike);
            option.setExpiration(expiration);
            option.setSecurityFamily(securityFamily);
            option.setUnderlying(securityFamily.getUnderlying());

            // ignore options that already exist
            if (!existingOptions.contains(option)) {
                newOptions.add(option);
            }
        }

        getOptionDao().create(newOptions);

        logger.debug("retrieved options for optionfamily: " + securityFamily.getName() + " " + newOptions);
    }

    private void retrieveFutures(FutureFamily securityFamily, Set<ContractDetails> contractDetailsSet) throws Exception {

        // get all current futures
        Set<Future> existingFutures = new TreeSet<Future>(getComparator());
        existingFutures.addAll(getFutureDao().findBySecurityFamily(securityFamily.getId()));

        Set<Future> newFutures = new TreeSet<Future>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Future future = Future.Factory.newInstance();

            Contract contract = contractDetails.m_summary;
            Date expiration = format.parse(contract.m_expiry);

            String symbol = FutureSymbol.getSymbol(securityFamily, expiration);
            final String isin = FutureSymbol.getIsin(securityFamily, expiration);
            String ric = FutureSymbol.getRic(securityFamily, expiration);
            String conid = String.valueOf(contract.m_conId);

            future.setSymbol(symbol);
            future.setIsin(isin);
            future.setRic(ric);
            future.setConid(conid);
            future.setExpiration(expiration);
            future.setSecurityFamily(securityFamily);
            future.setUnderlying(securityFamily.getUnderlying());

            // ignore futures that already exist
            if (!existingFutures.contains(future)) {
                newFutures.add(future);
            }
        }

        getFutureDao().create(newFutures);

        logger.debug("retrieved futures for futurefamily: " + securityFamily.getName() + " " + newFutures);
    }

    private void retrieveStocks(SecurityFamily securityFamily, Set<ContractDetails> contractDetailsSet) throws Exception {

        // get all current stocks
        Map<String,Stock> existingStocks = new HashMap<String,Stock>();

        for (Stock stock : getStockDao().findStocksBySecurityFamily(securityFamily.getId())) {
            existingStocks.put(stock.getSymbol(), stock);
        }

        // contractDetailsList most likely only contains one entry
        Set<Stock> newStocks = new TreeSet<Stock>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Contract contract = contractDetails.m_summary;

            String symbol = contract.m_symbol;
            String conid = String.valueOf(contract.m_conId);

            // update stocks (conid) that already exist
            if (existingStocks.containsKey(symbol)) {

                Stock stock = existingStocks.get(symbol);
                stock.setConid(conid);

            } else {

                Stock stock = Stock.Factory.newInstance();
                stock.setSymbol(symbol);
                stock.setConid(conid);
                stock.setSecurityFamily(securityFamily);
                stock.setUnderlying(securityFamily.getUnderlying());

                newStocks.add(stock);
            }
        }

        getStockDao().create(newStocks);
    }

    private Comparator<Security> getComparator() {

        // comparator based on conid
        Comparator<Security> comparator = new Comparator<Security>() {
            @Override
            public int compare(Security o1, Security o2) {
                return o1.getConid().compareTo(o2.getConid());
            }
        };
        return comparator;
    }
}
