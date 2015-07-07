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
package ch.algotrader.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.math.util.MathUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.CoreConfig;
import ch.algotrader.dao.SubscriptionDao;
import ch.algotrader.dao.security.ForexDao;
import ch.algotrader.dao.security.FutureFamilyDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.strategy.Strategy;
import ch.algotrader.entity.strategy.StrategyImpl;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Side;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.BalanceVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Transactional
public class ForexServiceImpl implements ForexService {

    private static final Logger LOGGER = LogManager.getLogger(ForexServiceImpl.class);
    private static final Logger NOTIFICATION_LOGGER = LogManager.getLogger("ch.algotrader.service.NOTIFICATION");

    private final CommonConfig commonConfig;

    private final CoreConfig coreConfig;

    private final OrderService orderService;

    private final PortfolioService portfolioService;

    private final LookupService lookupService;

    private final FutureService futureService;

    private final MarketDataService marketDataService;

    private final ForexDao forexDao;

    private final StrategyDao strategyDao;

    private final SubscriptionDao subscriptionDao;

    private final FutureFamilyDao futureFamilyDao;

    private final EngineManager engineManager;

    public ForexServiceImpl(final CommonConfig commonConfig,
            final CoreConfig coreConfig,
            final OrderService orderService,
            final PortfolioService portfolioService,
            final LookupService lookupService,
            final FutureService futureService,
            final MarketDataService marketDataService,
            final ForexDao forexDao,
            final StrategyDao strategyDao,
            final SubscriptionDao subscriptionDao,
            final FutureFamilyDao futureFamilyDao,
            final EngineManager engineManager) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(coreConfig, "CoreConfig is null");
        Validate.notNull(orderService, "OrderService is null");
        Validate.notNull(portfolioService, "PortfolioService is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(futureService, "FutureService is null");
        Validate.notNull(marketDataService, "MarketDataService is null");
        Validate.notNull(forexDao, "ForexDao is null");
        Validate.notNull(strategyDao, "StrategyDao is null");
        Validate.notNull(subscriptionDao, "SubscriptionDao is null");
        Validate.notNull(futureFamilyDao, "FutureFamilyDao is null");
        Validate.notNull(engineManager, "EngineManager is null");

        this.commonConfig = commonConfig;
        this.coreConfig = coreConfig;
        this.orderService = orderService;
        this.portfolioService = portfolioService;
        this.lookupService = lookupService;
        this.futureService = futureService;
        this.marketDataService = marketDataService;
        this.forexDao = forexDao;
        this.strategyDao = strategyDao;
        this.subscriptionDao = subscriptionDao;
        this.futureFamilyDao = futureFamilyDao;
        this.engineManager = engineManager;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void hedgeForex() {

        Strategy server = this.strategyDao.findServer();

        CoreConfig coreConfig = this.coreConfig;
        // potentially close a Forex Future position if it is below the MinTimeToExpiration
        if (coreConfig.isFxFutureHedgeEnabled()) {

            // get the closing orders
            final List<Order> orders = new ArrayList<>();
            for (Position position : this.lookupService.getOpenPositionsByStrategyTypeAndUnderlyingType(StrategyImpl.SERVER, Future.class, Forex.class)) {

                // check if expiration is below minimum
                Future future = (Future) position.getSecurity();

                Forex forex = (Forex) future.getUnderlying();

                Subscription forexSubscription = this.subscriptionDao.findByStrategyAndSecurity(StrategyImpl.SERVER, forex.getId());
                if (!forexSubscription.hasProperty("hedgingFamily")) {
                    throw new IllegalStateException("no hedgingFamily defined for forex " + forex);
                }

                FutureFamily futureFamily = this.futureFamilyDao.load(forexSubscription.getIntProperty("hedgingFamily"));
                if (!future.getSecurityFamily().equals(futureFamily)) {
                    // continue if forex is not hedged with this futureFamily
                    continue;
                }

                if (future.getTimeToExpiration(this.engineManager.getCurrentEPTime()) < coreConfig.getFxFutureHedgeMinTimeToExpiration()) {

                    Order order = this.orderService.createOrderByOrderPreference(coreConfig.getFxHedgeOrderPreference());
                    order.setStrategy(server);
                    order.setSecurity(future);
                    order.setQuantity(Math.abs(position.getQuantity()));
                    order.setSide(position.getQuantity() > 0 ? Side.SELL : Side.BUY);

                    orders.add(order);
                }
            }

            // setup an TradeCallback so that new hedge positions are only setup when existing positions are closed
            if (orders.size() > 0) {

                if (NOTIFICATION_LOGGER.isInfoEnabled()) {
                    NOTIFICATION_LOGGER.info("{} fx hedging position(s) have been closed due to approaching expiration, please run equalizeForex again", orders.size());
                }
                // send the orders
                for (Order order : orders) {
                    this.orderService.sendOrder(order);
                }

                return; // do not go any furter because closing trades will have to finish first
            }
        }

        // process all non-base currency balances
        Collection<BalanceVO> balances = this.portfolioService.getBalances();
        for (BalanceVO balance : balances) {

            Currency portfolioBaseCurrency = this.commonConfig.getPortfolioBaseCurrency();
            if (balance.getCurrency().equals(portfolioBaseCurrency)) {
                continue;
            }

            // get the netLiqValueBase
            double netLiqValue = balance.getNetLiqValue().doubleValue();
            double netLiqValueBase = balance.getExchangeRate() * netLiqValue;

            // check if amount is larger than minimum
            if (Math.abs(netLiqValueBase) >= coreConfig.getFxHedgeMinAmount()) {

                // get the forex
                Forex forex = this.forexDao.getForex(portfolioBaseCurrency, balance.getCurrency());

                double tradeValue = forex.getBaseCurrency().equals(portfolioBaseCurrency) ? netLiqValueBase : netLiqValue;

                // create the order
                Order order = this.orderService.createOrderByOrderPreference(coreConfig.getFxHedgeOrderPreference());
                order.setStrategy(server);

                // if a hedging family is defined for this Forex use it instead of the Forex directly
                int qty;
                if (coreConfig.isFxFutureHedgeEnabled()) {

                    Subscription forexSubscription = this.subscriptionDao.findByStrategyAndSecurity(StrategyImpl.SERVER, forex.getId());
                    if (!forexSubscription.hasProperty("hedgingFamily")) {
                        throw new IllegalStateException("no hedgingFamily defined for forex " + forex);
                    }

                    FutureFamily futureFamily = this.futureFamilyDao.load(forexSubscription.getIntProperty("hedgingFamily"));

                    Date targetDate = DateUtils.addMilliseconds(this.engineManager.getCurrentEPTime(), coreConfig.getFxFutureHedgeMinTimeToExpiration());
                    Future future = this.futureService.getFutureByMinExpiration(futureFamily.getId(), targetDate);

                    // make sure the future is subscriped
                    this.marketDataService.subscribe(server.getName(), future.getId());

                    order.setSecurity(future);

                    // round to the number of contracts
                    qty = (int) MathUtils.round(tradeValue / futureFamily.getContractSize(), 0);

                } else {

                    order.setSecurity(forex);

                    // round to batchSize
                    qty = (int) RoundUtil.roundToNextN(tradeValue, coreConfig.getFxHedgeBatchSize());
                }

                if (forex.getBaseCurrency().equals(portfolioBaseCurrency)) {

                    // expected case
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.BUY : Side.SELL);

                } else {

                    // reverse case
                    order.setQuantity(Math.abs(qty));
                    order.setSide(qty > 0 ? Side.SELL : Side.BUY);
                }

                this.orderService.sendOrder(order);

            } else {

                if (LOGGER.isInfoEnabled()) {
                    LOGGER.info("no forex hedge is performed on {} because amount {} is below {}", balance.getCurrency(), RoundUtil.getBigDecimal(Math.abs(netLiqValueBase)),
                            coreConfig.getFxHedgeMinAmount());
                }
                continue;
            }
        }

    }

}
