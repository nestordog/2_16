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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.ib.IBIdGenerator;
import ch.algotrader.adapter.ib.IBPendingRequests;
import ch.algotrader.adapter.ib.IBSession;
import ch.algotrader.concurrent.Promise;
import ch.algotrader.concurrent.PromiseImpl;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.dao.security.StockDao;
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
import ch.algotrader.service.ExternalServiceException;
import ch.algotrader.service.ReferenceDataServiceImpl;
import ch.algotrader.service.ServiceException;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.RoundUtil;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBNativeReferenceDataServiceImpl extends ReferenceDataServiceImpl implements IBNativeReferenceDataService {

    private static final Logger LOGGER = LogManager.getLogger(IBNativeReferenceDataServiceImpl.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#######");

    private final IBSession iBSession;

    private final IBPendingRequests pendingRequests;

    private final IBIdGenerator idGenerator;

    private final OptionDao optionDao;

    private final FutureDao futureDao;

    private final SecurityFamilyDao securityFamilyDao;

    private final StockDao stockDao;

    public IBNativeReferenceDataServiceImpl(
            final IBSession iBSession,
            final IBPendingRequests pendingRequests,
            final IBIdGenerator idGenerator,
            final OptionDao optionDao,
            final FutureDao futureDao,
            final SecurityFamilyDao securityFamilyDao,
            final StockDao stockDao) {

        Validate.notNull(iBSession, "IBSession is null");
        Validate.notNull(pendingRequests, "IBPendingRequests is null");
        Validate.notNull(idGenerator, "IBIdGenerator is null");
        Validate.notNull(optionDao, "OptionDao is null");
        Validate.notNull(futureDao, "FutureDao is null");
        Validate.notNull(securityFamilyDao, "SecurityFamilyDao is null");
        Validate.notNull(stockDao, "StockDao is null");

        this.iBSession = iBSession;
        this.pendingRequests = pendingRequests;
        this.idGenerator = idGenerator;
        this.optionDao = optionDao;
        this.futureDao = futureDao;
        this.securityFamilyDao = securityFamilyDao;
        this.stockDao = stockDao;
    }

    @Override
    public void retrieve(long securityFamilyId) {

        SecurityFamily securityFamily = this.securityFamilyDao.get(securityFamilyId);

        int requestId = this.idGenerator.getNextRequestId();
        Contract contract = new Contract();

        contract.m_symbol = securityFamily.getSymbolRoot(Broker.IB);

        contract.m_currency = securityFamily.getCurrency().toString();

        contract.m_exchange = securityFamily.getExchange().getIbCode();

        contract.m_multiplier = DECIMAL_FORMAT.format(securityFamily.getContractSize(Broker.IB));

        if (securityFamily instanceof OptionFamily) {
            contract.m_secType = "OPT";
        } else if (securityFamily instanceof FutureFamily) {
            contract.m_secType = "FUT";
        } else {
            throw new IllegalStateException("illegal securityFamily type");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request contract details; request id = {}; symbol = {}", requestId, contract.m_symbol);
        }

        PromiseImpl<List<ContractDetails>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addContractDetailRequest(requestId, promise);
        this.iBSession.reqContractDetails(requestId, contract);

        Set<ContractDetails> contractDetailsSet = getContractDetailsBlocking(promise);
        try {
            if (securityFamily instanceof OptionFamily) {
                retrieveOptions((OptionFamily) securityFamily, contractDetailsSet);
            } else if (securityFamily instanceof FutureFamily) {
                retrieveFutures((FutureFamily) securityFamily, contractDetailsSet);
            } else {
                throw new IllegalStateException("illegal securityFamily type");
            }
        } catch (ParseException ex) {
            throw new ExternalServiceException(ex);
        }
    }

    @Override
    public void retrieveStocks(long securityFamilyId, String symbol) {

        Validate.notEmpty(symbol, "Symbol is empty");

        SecurityFamily securityFamily = this.securityFamilyDao.get(securityFamilyId);

        int requestId = this.idGenerator.getNextRequestId();
        Contract contract = new Contract();

        contract.m_symbol = symbol;

        contract.m_currency = securityFamily.getCurrency().toString();

        contract.m_exchange = securityFamily.getExchange().getIbCode();

        contract.m_secType = "STK";

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Request stock details; request id = {}; symbol = {}", requestId, contract.m_symbol);
        }

        PromiseImpl<List<ContractDetails>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addContractDetailRequest(requestId, promise);
        this.iBSession.reqContractDetails(requestId, contract);

        Set<ContractDetails> contractDetailsSet = getContractDetailsBlocking(promise);
        retrieveStocks(securityFamily, contractDetailsSet);

    }

    private Set<ContractDetails> getContractDetailsBlocking(final Promise<List<ContractDetails>> promise) {
        try {
            return new HashSet<>(promise.get());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        } catch (ExecutionException ex) {
            throw IBNativeSupport.rethrow(ex.getCause());
        }
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
            LocalDate expirationDate = DATE_FORMAT.parse(contract.m_expiry, LocalDate::from);

            final String isin = OptionSymbol.getIsin(securityFamily, expirationDate, type, strike);
            String symbol = OptionSymbol.getSymbol(securityFamily, expirationDate, type, strike, false);
            String ric = OptionSymbol.getRic(securityFamily, expirationDate, type, strike);
            String conid = String.valueOf(contract.m_conId);

            option.setSymbol(symbol);
            option.setIsin(isin);
            option.setRic(ric);
            option.setConid(conid);
            option.setType(type);
            option.setStrike(strike);
            option.setExpiration(DateTimeLegacy.toLocalDate(expirationDate));
            option.setSecurityFamily(securityFamily);
            option.setUnderlying(securityFamily.getUnderlying());

            // ignore options that already exist
            if (!existingOptions.contains(option)) {
                newOptions.add(option);
            }
        }

        this.optionDao.saveAll(newOptions);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("retrieved options for optionfamily: {} {}", securityFamily.getName(), newOptions);
        }
    }

    private void retrieveFutures(FutureFamily securityFamily, Set<ContractDetails> contractDetailsSet) throws ParseException {

        // get all current futures
        Set<Future> existingFutures = new TreeSet<>(getComparator());
        existingFutures.addAll(this.futureDao.findBySecurityFamily(securityFamily.getId()));

        Set<Future> newFutures = new TreeSet<>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Future future = Future.Factory.newInstance();

            Contract contract = contractDetails.m_summary;
            LocalDate expiration = DATE_FORMAT.parse(contract.m_expiry, LocalDate::from);
            LocalDate contractMonth = MONTH_FORMAT.parse(contractDetails.m_contractMonth, LocalDate::from);

            String symbol = FutureSymbol.getSymbol(securityFamily, contractMonth);
            final String isin = FutureSymbol.getIsin(securityFamily, contractMonth);
            String ric = FutureSymbol.getRic(securityFamily, contractMonth);
            String conid = String.valueOf(contract.m_conId);

            future.setSymbol(symbol);
            future.setIsin(isin);
            future.setRic(ric);
            future.setConid(conid);
            future.setExpiration(DateTimeLegacy.toLocalDate(expiration));
            future.setSecurityFamily(securityFamily);
            future.setUnderlying(securityFamily.getUnderlying());

            // ignore futures that already exist
            if (!existingFutures.contains(future)) {
                newFutures.add(future);
            }
        }

        this.futureDao.saveAll(newFutures);

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("retrieved futures for futurefamily: {} {}", securityFamily.getName(), newFutures);
        }
    }

    private void retrieveStocks(SecurityFamily securityFamily, Set<ContractDetails> contractDetailsSet) {

        // get all current stocks
        Map<String, Stock> existingStocks = new HashMap<>();

        for (Stock stock : this.stockDao.findStocksBySecurityFamily(securityFamily.getId())) {
            existingStocks.put(stock.getSymbol(), stock);
        }

        // contractDetailsList most likely only contains one entry
        Set<Stock> newStocks = new HashSet<>();
        for (ContractDetails contractDetails : contractDetailsSet) {

            Contract contract = contractDetails.m_summary;

            String symbol = contract.m_symbol;
            String conid = String.valueOf(contract.m_conId);
            String description = contractDetails.m_longName;

            // update stocks (conid) that already exist
            if (existingStocks.containsKey(symbol)) {

                Stock stock = existingStocks.get(symbol);
                stock.setConid(conid);

            } else {

                Stock stock = Stock.Factory.newInstance();
                stock.setSymbol(symbol);
                stock.setDescription(description);
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
        Comparator<Security> comparator = (o1, o2) -> {
            if (o1.getConid() != null && o2.getConid() != null) {
                return o1.getConid().compareTo(o2.getConid());
            } else {
                return o1.getSymbol().compareTo(o2.getSymbol());
            }
        };
        return comparator;
    }
}
