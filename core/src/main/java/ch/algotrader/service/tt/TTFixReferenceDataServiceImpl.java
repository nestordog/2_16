/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service.tt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.tt.TTPendingRequests;
import ch.algotrader.adapter.tt.TTSecurityDefVO;
import ch.algotrader.adapter.tt.TTSecurityDefinitionRequestFactory;
import ch.algotrader.adapter.tt.TTSecurityDefinitionRequestIdGenerator;
import ch.algotrader.concurrent.Promise;
import ch.algotrader.concurrent.PromiseImpl;
import ch.algotrader.dao.security.FutureDao;
import ch.algotrader.dao.security.OptionDao;
import ch.algotrader.dao.security.SecurityFamilyDao;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.NoServiceResponseException;
import ch.algotrader.service.ReferenceDataService;
import ch.algotrader.service.ServiceException;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.DateTimePatterns;
import ch.algotrader.util.RoundUtil;
import quickfix.field.SecurityType;
import quickfix.fix42.SecurityDefinitionRequest;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
@InitializationPriority(InitializingServiceType.BROKER_INTERFACE)
@Transactional(propagation = Propagation.SUPPORTS)
public class TTFixReferenceDataServiceImpl implements ReferenceDataService, InitializingServiceI {

    private static final Logger LOGGER = LogManager.getLogger(TTFixReferenceDataServiceImpl.class);

    private static final DateTimeFormatter ICE_IPE_SYMBOL = new DateTimeFormatterBuilder()
            .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
            .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
            .toFormatter(Locale.ROOT);

    private final FixAdapter fixAdapter;
    private final ExternalSessionStateHolder stateHolder;
    private final TTPendingRequests pendingRequests;
    private final OptionDao optionDao;
    private final FutureDao futureDao;
    private final SecurityFamilyDao securityFamilyDao;
    private final TTSecurityDefinitionRequestIdGenerator requestIdGenerator;
    private final TTSecurityDefinitionRequestFactory requestFactory;

    public TTFixReferenceDataServiceImpl(
            final FixAdapter fixAdapter,
            final ExternalSessionStateHolder stateHolder,
            final TTPendingRequests pendingRequests,
            final OptionDao optionDao,
            final FutureDao futureDao,
            final SecurityFamilyDao securityFamilyDao) {

        Validate.notNull(fixAdapter, "FixAdapter is null");
        Validate.notNull(stateHolder, "ExternalSessionStateHolder is null");
        Validate.notNull(pendingRequests, "IBPendingRequests is null");
        Validate.notNull(optionDao, "OptionDao is null");
        Validate.notNull(futureDao, "FutureDao is null");
        Validate.notNull(securityFamilyDao, "SecurityFamilyDao is null");

        this.fixAdapter = fixAdapter;
        this.stateHolder = stateHolder;
        this.pendingRequests = pendingRequests;
        this.optionDao = optionDao;
        this.futureDao = futureDao;
        this.securityFamilyDao = securityFamilyDao;
        this.requestIdGenerator = new TTSecurityDefinitionRequestIdGenerator();
        this.requestFactory = new TTSecurityDefinitionRequestFactory();
    }

    @Override
    public void init() {

        this.fixAdapter.openSession(this.stateHolder.getName());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void retrieve(final long securityFamilyId) {

        SecurityFamily securityFamily = this.securityFamilyDao.get(securityFamilyId);
        if (securityFamily == null) {
            throw new IllegalArgumentException("Unknown security family id: " + securityFamilyId);
        }

        String requestId = this.requestIdGenerator.generateId(securityFamily);
        String securityType;
        if (securityFamily instanceof OptionFamily) {
            securityType = SecurityType.OPTION;
        } else if (securityFamily instanceof FutureFamily) {
            securityType = SecurityType.FUTURE;
        } else {
            securityType = SecurityType.NO_SECURITY_TYPE;
        }

        SecurityDefinitionRequest request = this.requestFactory.create(requestId, securityFamily, securityType);

        PromiseImpl<List<TTSecurityDefVO>> promise = new PromiseImpl<>(null);
        this.pendingRequests.addSecurityDefinitionRequest(requestId, promise);

        this.fixAdapter.sendMessage(request, this.stateHolder.getName());

        List<TTSecurityDefVO> securityDefs = getSecurityDefsBlocking(promise);
        if (securityFamily instanceof OptionFamily) {
            retrieveOptions((OptionFamily) securityFamily, securityDefs);
        } else if (securityFamily instanceof FutureFamily) {
            retrieveFutures((FutureFamily) securityFamily, securityDefs);
        } else {
            throw new ServiceException("TT does not support retrieval of security definitions for " + securityFamily.getClass());
        }
    }

    private List<TTSecurityDefVO> getSecurityDefsBlocking(final Promise<List<TTSecurityDefVO>> promise) {
        try {
            return promise.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new ServiceException(ex);
        } catch (TimeoutException e) {
            throw new NoServiceResponseException("No response from TT after 10 seconds");
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            throw new ServiceException(cause != null ? cause.getMessage() : "Unexpected exception", cause);
        }
    }

    private void retrieveOptions(final OptionFamily securityFamily, final List<TTSecurityDefVO> securityDefs) {

        // get all current options
        List<Option> allOptions = this.optionDao.findBySecurityFamily(securityFamily.getId());
        Map<String, Option> mapByTtid = allOptions.stream()
                .filter(e -> e.getTtid() != null)
                .collect(Collectors.toMap(e -> e.getTtid(), e -> e));
        Map<String, Option> mapBySymbol = allOptions.stream()
                .collect(Collectors.toMap(e -> e.getSymbol(), e -> e));
        for (TTSecurityDefVO securityDef: securityDefs) {

            String type = securityDef.getType();
            if (!type.equalsIgnoreCase("OPT")) {
                throw new ServiceException("Unexpected security definition type for option: " + type);
            }
            String id = securityDef.getId();
            if (!mapByTtid.containsKey(id)) {
                OptionType optionType = securityDef.getOptionType();
                BigDecimal strike = securityDef.getStrikePrice() != null ? RoundUtil.getBigDecimal(
                        securityDef.getStrikePrice() / securityFamily.getPriceMultiplier(Broker.TT.name()),
                        securityFamily.getScale(Broker.TT.name())) : null;
                LocalDate expiryDate = securityDef.getExpiryDate() != null ? securityDef.getExpiryDate() : securityDef.getMaturityDate();
                String symbol = OptionSymbol.getSymbol(securityFamily, expiryDate, optionType, strike, false);

                if (!mapBySymbol.containsKey(symbol)) {
                    String isin = securityFamily.getIsinRoot() != null ? OptionSymbol.getIsin(securityFamily, expiryDate, optionType, strike) : null;
                    String ric = securityFamily.getRicRoot() != null ? OptionSymbol.getRic(securityFamily, expiryDate, optionType, strike) : null;
                    String desc = securityDef.getDescription();

                    Option option = Option.Factory.newInstance();
                    option.setDescription(desc);
                    option.setSymbol(symbol);
                    option.setIsin(isin);
                    option.setRic(ric);
                    option.setTtid(id);
                    option.setOptionType(optionType);
                    option.setStrike(strike);
                    option.setExpiration(DateTimeLegacy.toLocalDate(expiryDate));
                    option.setSecurityFamily(securityFamily);
                    option.setUnderlying(securityFamily.getUnderlying());

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Creating option based on TT definition: {} {} {} {}",
                                securityDef.getSymbol(),
                                securityDef.getOptionType(),
                                securityDef.getMaturityDate(),
                                securityDef.getDescription());
                    }
                    this.optionDao.save(option);
                } else {
                    Option option = mapBySymbol.get(symbol);
                    option.setTtid(id);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Updating option based on TT definition: {}",securityDef.getSymbol());
                    }
                }
            }
        }
    }

    private void retrieveFutures(final FutureFamily securityFamily, final List<TTSecurityDefVO> securityDefs) {

        // get all current futures
        List<Future> allFutures = this.futureDao.findBySecurityFamily(securityFamily.getId());
        Map<String, Future> mapByTtid = allFutures.stream()
                .filter(e -> e.getTtid() != null)
                .collect(Collectors.toMap(e -> e.getTtid(), e -> e));
        Map<String, Future> mapBySymbol = allFutures.stream()
                .collect(Collectors.toMap(e -> e.getSymbol(), e -> e));

        for (TTSecurityDefVO securityDef: securityDefs) {

            String type = securityDef.getType();
            if (!type.equalsIgnoreCase("FUT")) {
                throw new ServiceException("Unexpected security definition type for future: " + type);
            }
            String id = securityDef.getId();
            if (!mapByTtid.containsKey(id)) {

                LocalDate maturityDate = securityDef.getMaturityDate();
                LocalDate expiration = maturityDate.withDayOfMonth(1);

                // IPE e-Brent has to be handled as a special case as it happens to have multiple contracts
                // with the same expiration month
                String symbol;
                if ("ICE_IPE".equalsIgnoreCase(securityDef.getExchange()) && securityDef.getAltSymbol() != null) {
                    String altSymbol = securityDef.getAltSymbol();
                    if (altSymbol.startsWith("Q") || altSymbol.startsWith("Cal")) {
                        continue;
                    } else {
                        try {
                            TemporalAccessor parsed = ICE_IPE_SYMBOL.parse(altSymbol);
                            int year = parsed.get(ChronoField.YEAR);
                            int month = parsed.get(ChronoField.MONTH_OF_YEAR);
                            expiration = LocalDate.of(year, month, 1);
                            symbol = FutureSymbol.getSymbol(securityFamily, expiration);
                        } catch (DateTimeParseException ex) {
                            throw new ServiceException("Unable to parse IPE e-Brent expiration month / year: " + altSymbol, ex);
                        }
                    }
                } else {
                    symbol = FutureSymbol.getSymbol(securityFamily, maturityDate);
                }
                if (!mapBySymbol.containsKey(symbol)) {
                    Future future = Future.Factory.newInstance();
                    String isin = securityFamily.getIsinRoot() != null ? FutureSymbol.getIsin(securityFamily, maturityDate) : null;
                    String ric = securityFamily.getRicRoot() != null ? FutureSymbol.getRic(securityFamily, maturityDate) : null;

                    future.setSymbol(symbol);
                    future.setIsin(isin);
                    future.setRic(ric);
                    future.setTtid(id);
                    future.setExpiration(DateTimeLegacy.toLocalDate(expiration));
                    future.setMonthYear(DateTimePatterns.MONTH_YEAR.format(maturityDate));
                    future.setSecurityFamily(securityFamily);
                    future.setUnderlying(securityFamily.getUnderlying());

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Creating future based on TT definition: {} {}",
                                securityDef.getSymbol(), securityDef.getMaturityDate());
                    }
                    this.futureDao.save(future);
                } else {
                    Future future = mapBySymbol.get(symbol);
                    future.setTtid(id);

                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Updating future based on TT definition: {} {}",
                            securityDef.getSymbol(), securityDef.getMaturityDate());
                    }
                }
            }
        }
    }

    @Override
    public void retrieveStocks(final long securityFamilyId, final String symbol) {
        throw new ServiceException("TT does not support retrieval of stock definitions");
    }

}
