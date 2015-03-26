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
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyDao;
import ch.algotrader.entity.security.Stock;
import ch.algotrader.entity.security.StockDao;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.service.ReferenceDataServiceImpl;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeReferenceDataServiceImpl extends ReferenceDataServiceImpl implements IBNativeReferenceDataService {

    private static final Logger logger = LogManager.getLogger(IBNativeReferenceDataServiceImpl.class.getName());
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.#######");

    private final BlockingQueue<ContractDetails> contractDetailsQueue;

    private final IBSession iBSession;

    private final IBIdGenerator iBIdGenerator;

    private final OptionDao optionDao;

    private final FutureDao futureDao;

    private final SecurityFamilyDao securityFamilyDao;

    private final StockDao stockDao;

    public IBNativeReferenceDataServiceImpl(final BlockingQueue<ContractDetails> contractDetailsQueue,
            final IBSession iBSession,
            final IBIdGenerator iBIdGenerator,
            final OptionDao optionDao,
            final FutureDao futureDao,
            final SecurityFamilyDao securityFamilyDao,
            final StockDao stockDao) {

        Validate.notNull(contractDetailsQueue, "ContractDetailsQueue is null");
        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(iBIdGenerator, "IBIdGenerator is null");
        Validate.notNull(optionDao, "OptionDao is null");
        Validate.notNull(futureDao, "FutureDao is null");
        Validate.notNull(securityFamilyDao, "SecurityFamilyDao is null");
        Validate.notNull(stockDao, "StockDao is null");

        this.contractDetailsQueue = contractDetailsQueue;
        this.iBSession = iBSession;
        this.iBIdGenerator = iBIdGenerator;
        this.optionDao = optionDao;
        this.futureDao = futureDao;
        this.securityFamilyDao = securityFamilyDao;
        this.stockDao = stockDao;
    }

    @Override
    public void retrieve(int securityFamilyId) {

        SecurityFamily securityFamily = this.securityFamilyDao.get(securityFamilyId);

        int requestId = this.iBIdGenerator.getNextRequestId();
        Contract contract = new Contract();

        contract.m_symbol = securityFamily.getSymbolRoot(Broker.IB);

        contract.m_currency = securityFamily.getCurrency().toString();

        contract.m_exchange = securityFamily.getExchangeCode(Broker.IB);

        contract.m_multiplier = decimalFormat.format(securityFamily.getContractSize(Broker.IB));

        if (securityFamily instanceof OptionFamily) {
            contract.m_secType = "OPT";
        } else if (securityFamily instanceof FutureFamily) {
            contract.m_secType = "FUT";
        } else {
            throw new IllegalArgumentException("illegal securityFamily type");
        }

        // send request
        this.iBSession.reqContractDetails(requestId, contract);

        Set<ContractDetails> contractDetailsSet;
        try {
            contractDetailsSet = retrieveContractDetails();

            if (securityFamily instanceof OptionFamily) {
                retrieveOptions((OptionFamily) securityFamily, contractDetailsSet);
            } else if (securityFamily instanceof FutureFamily) {
                retrieveFutures((FutureFamily) securityFamily, contractDetailsSet);
            } else {
                throw new IllegalArgumentException("illegal securityFamily type");
            }
        } catch (IllegalArgumentException | ParseException ex) {
            throw new IBNativeReferenceDataServiceException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IBNativeReferenceDataServiceException(ex);
        }
    }

    @Override
    public void retrieveStocks(int securityFamilyId, String symbol) {

        Validate.notEmpty(symbol, "Symbol is empty");

        SecurityFamily securityFamily = this.securityFamilyDao.get(securityFamilyId);

        int requestId = this.iBIdGenerator.getNextRequestId();
        Contract contract = new Contract();

        contract.m_symbol = symbol;

        contract.m_currency = securityFamily.getCurrency().toString();

        contract.m_exchange = securityFamily.getExchangeCode(Broker.IB);

        contract.m_secType = "STK";

        logger.debug("retrieving stock " + symbol);

        this.iBSession.reqContractDetails(requestId, contract);

        Set<ContractDetails> contractDetailsSet;
        try {
            contractDetailsSet = retrieveContractDetails();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IBNativeReferenceDataServiceException(ex);
        }
        retrieveStocks(securityFamily, contractDetailsSet);

    }

    private Set<ContractDetails> retrieveContractDetails() throws InterruptedException {

        Set<ContractDetails> contractDetailsSet = new HashSet<>();

        ContractDetails contractDetails;
        while (!((contractDetails = this.contractDetailsQueue.take()).m_summary.m_symbol == null)) {
            contractDetailsSet.add(contractDetails);
        }

        return contractDetailsSet;
    }

    private void retrieveOptions(OptionFamily securityFamily, Set<ContractDetails> contractDetailsSet) throws ParseException {

        // get all current options
        Set<Security> existingOptions = new TreeSet<>(getComparator());
        existingOptions.addAll(this.optionDao.findBySecurityFamily(securityFamily.getId()));

        Set<Option> newOptions = new TreeSet<>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Option option = Option.Factory.newInstance();

            Contract contract = contractDetails.m_summary;
            OptionType type = "C".equals(contract.m_right) ? OptionType.CALL : OptionType.PUT;
            BigDecimal strike = RoundUtil.getBigDecimal(contract.m_strike, securityFamily.getScale(Broker.IB));
            Date expiration = dayFormat.parse(contract.m_expiry);

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

        this.optionDao.saveAll(newOptions);

        logger.debug("retrieved options for optionfamily: " + securityFamily.getName() + " " + newOptions);
    }

    private void retrieveFutures(FutureFamily securityFamily, Set<ContractDetails> contractDetailsSet) throws ParseException {

        // get all current futures
        Set<Future> existingFutures = new TreeSet<>(getComparator());
        existingFutures.addAll(this.futureDao.findBySecurityFamily(securityFamily.getId()));

        Set<Future> newFutures = new TreeSet<>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Future future = Future.Factory.newInstance();

            Contract contract = contractDetails.m_summary;
            Date expiration = dayFormat.parse(contract.m_expiry);
            Date contractMonth = monthFormat.parse(contractDetails.m_contractMonth);

            String symbol = FutureSymbol.getSymbol(securityFamily, contractMonth);
            final String isin = FutureSymbol.getIsin(securityFamily, contractMonth);
            String ric = FutureSymbol.getRic(securityFamily, contractMonth);
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

        this.futureDao.saveAll(newFutures);

        logger.debug("retrieved futures for futurefamily: " + securityFamily.getName() + " " + newFutures);
    }

    private void retrieveStocks(SecurityFamily securityFamily, Set<ContractDetails> contractDetailsSet) {

        // get all current stocks
        Map<String, Stock> existingStocks = new HashMap<>();

        for (Stock stock : this.stockDao.findStocksBySecurityFamily(securityFamily.getId())) {
            existingStocks.put(stock.getSymbol(), stock);
        }

        // contractDetailsList most likely only contains one entry
        Set<Stock> newStocks = new TreeSet<>();
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

        this.stockDao.saveAll(newStocks);
    }

    private Comparator<Security> getComparator() {

        // comparator based on conid
        return (o1, o2) -> o1.getConid().compareTo(o2.getConid());
    }
}
