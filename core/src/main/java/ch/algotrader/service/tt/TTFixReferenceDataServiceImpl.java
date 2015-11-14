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
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import ch.algotrader.entity.security.Security;
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
    public void retrieve(final long securityFamilyId) {

        if (!this.stateHolder.isLoggedOn()) {
            throw new ServiceException("Fix session is not logged on");
        }

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

    private static final Comparator<Security> SECURITY_COMPARATOR = (o1, o2) -> {
        if (o1.getTtid() != null && o2.getTtid() != null) {
            return o1.getTtid().compareTo(o2.getTtid());
        } else {
            return o1.getSymbol().compareTo(o2.getSymbol());
        }
    };

    private void retrieveOptions(final OptionFamily securityFamily, final List<TTSecurityDefVO> securityDefs) {

        // get all current options
        Set<Option> existingOptions = new TreeSet<>(SECURITY_COMPARATOR);
        existingOptions.addAll(this.optionDao.findBySecurityFamily(securityFamily.getId()));
        for (TTSecurityDefVO securityDef: securityDefs) {

            String type = securityDef.getType();
            if (!type.equalsIgnoreCase("OPT")) {
                throw new ServiceException("Unexpected security definition type for option: " + type);
            }
            String id = securityDef.getId();
            OptionType optionType = securityDef.getOptionType();
            BigDecimal strike = securityDef.getStrikePrice() != null ? RoundUtil.getBigDecimal(
                    securityDef.getStrikePrice(), securityFamily.getScale(Broker.TT.name())) : null;
            LocalDate expiryDate = securityDef.getExpiryDate() != null ? securityDef.getExpiryDate() : securityDef.getMaturityDate();
            String desc = securityDef.getDescription();

            String symbol = OptionSymbol.getSymbol(securityFamily, expiryDate, optionType, strike, false);
            String isin = securityFamily.getIsinRoot() != null ? OptionSymbol.getIsin(securityFamily, expiryDate, optionType, strike) : null;
            String ric = securityFamily.getRicRoot() != null ? OptionSymbol.getRic(securityFamily, expiryDate, optionType, strike) : null;

            Option option = Option.Factory.newInstance();
            option.setDescription(desc);
            option.setSymbol(symbol);
            option.setIsin(isin);
            option.setRic(ric);
            option.setTtid(id);
            option.setType(optionType);
            option.setStrike(strike);
            option.setExpiration(DateTimeLegacy.toLocalDate(expiryDate));
            option.setSecurityFamily(securityFamily);
            option.setUnderlying(securityFamily.getUnderlying());

            // ignore options that already exist
            if (!existingOptions.contains(option)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating option based on TT definition: {} {} {} {}",
                            securityDef.getSymbol(),
                            securityDef.getOptionType(),
                            securityDef.getMaturityDate(),
                            securityDef.getDescription());
                }
                this.optionDao.save(option);
            }
        }
    }

    private void retrieveFutures(final FutureFamily securityFamily, final List<TTSecurityDefVO> securityDefs) {

        // get all current futures
        Set<Future> existingFutures = new TreeSet<>(SECURITY_COMPARATOR);
        existingFutures.addAll(this.futureDao.findBySecurityFamily(securityFamily.getId()));

        for (TTSecurityDefVO securityDef: securityDefs) {

            String type = securityDef.getType();
            if (!type.equalsIgnoreCase("FUT")) {
                throw new ServiceException("Unexpected security definition type for future: " + type);
            }
            String id = securityDef.getId();
            LocalDate maturityDate = securityDef.getMaturityDate();
            LocalDate expiryDate = securityDef.getExpiryDate();

            Future future = Future.Factory.newInstance();

            // IPE e-Brent has to be handled as a special case as it happens to have multiple contracts
            // with the same expiration month
            String symbol;
            if ("IPE e-Brent".equalsIgnoreCase(securityFamily.getSymbolRoot()) && securityDef.getAltSymbol() != null) {
                symbol = securityFamily.getSymbolRoot() + " " + securityDef.getAltSymbol();
            } else {
                symbol = FutureSymbol.getSymbol(securityFamily, maturityDate);
            }
            String isin = securityFamily.getIsinRoot() != null ? FutureSymbol.getIsin(securityFamily, maturityDate) : null;
            String ric = securityFamily.getRicRoot() != null ? FutureSymbol.getRic(securityFamily, maturityDate) : null;

            future.setSymbol(symbol);
            future.setIsin(isin);
            future.setRic(ric);
            future.setTtid(id);
            future.setExpiration(DateTimeLegacy.toLocalDate(maturityDate));
            if (expiryDate != null) {
                future.setFirstNotice(DateTimeLegacy.toLocalDate(expiryDate));
                future.setLastTrading(DateTimeLegacy.toLocalDate(expiryDate));
            }
            future.setSecurityFamily(securityFamily);
            future.setUnderlying(securityFamily.getUnderlying());

            // ignore futures that already exist
            if (!existingFutures.contains(future)) {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Creating future based on TT definition: {} {}",
                            securityDef.getSymbol(), securityDef.getMaturityDate());
                }
                this.futureDao.save(future);
            }
        }
    }

    @Override
    public void retrieveStocks(final long securityFamilyId, final String symbol) {
        throw new ServiceException("TT does not support retrieval of stock definitions");
    }

}
