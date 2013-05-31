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
package ch.algotrader.entity.security;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.esper.EsperManager;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.metric.MetricsUtil;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Currency;
import com.espertech.esper.event.WrapperEventBean;
import com.espertech.esper.event.bean.BeanEventBean;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    private static Logger logger = MyLogger.getLogger(SecurityImpl.class.getName());

    private static @Value("#{T(ch.algorader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private static @Value("${misc.initialMarginMarkup}") double initialMarginMarkup;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MarketDataEvent getCurrentMarketDataEvent() {

        String startedStrategyName = ServiceLocator.instance().getConfiguration().getStartedStrategyName();
        if (EsperManager.isInitialized(startedStrategyName)) {
            List<Map> events = EsperManager.getAllEvents(startedStrategyName, "CURRENT_MARKET_DATA_EVENT");

            // try to see if the rule CURRENT_MARKET_DATA_EVENT has any events
            for (Map event : events) {
                Integer securityId = (Integer) event.get("securityId");
                if (securityId.equals(getId())) {
                    Object obj = event.get("marketDataEvent");
                    if (obj instanceof WrapperEventBean) {
                        return (MarketDataEvent) ((WrapperEventBean) obj).getUnderlying();
                    } else {
                        return (MarketDataEvent) ((BeanEventBean) obj).getUnderlying();
                    }
                }
            }
        }

        if (getSecurityFamilyInitialized().isSynthetic()) {

            return null;
        } else {

            // if we did not get a marketDataEvent up to now go to the db an get the last tick
            Tick tick = ServiceLocator.instance().getLookupService().getLastTick(getId());

            if (tick == null) {
                logger.warn("no last tick was found for " + this);
            }

            return tick;
        }
    }

    @Override
    public boolean isSubscribed() {

        return Hibernate.isInitialized(getSubscriptions()) && (getSubscriptions().size() != 0);
    }

    @Override
    public double getFXRate(Currency transactionCurrency) {

        return ServiceLocator.instance().getLookupService().getForexRateDouble(getSecurityFamily().getCurrency(), transactionCurrency);
    }

    @Override
    public double getFXRateBase() {

        return getFXRate(portfolioBaseCurrency);
    }

    @Override
    public double getLeverage() {
        return 0;
    }

    /**
     * generic default margin
     */
    @Override
    public double getMargin() {

        MarketDataEvent marketDataEvent = getCurrentMarketDataEvent();

        double marginPerContract = 0;
        if (marketDataEvent != null && marketDataEvent.getCurrentValueDouble() > 0.0) {

            int contractSize = getSecurityFamily().getContractSize();
            marginPerContract = marketDataEvent.getCurrentValueDouble() * contractSize / initialMarginMarkup;
        } else {
            logger.warn("no last tick available or currentValue to low to set margin on " + this);
        }
        return marginPerContract;
    }

    @Override
    public String toString() {

        return getSymbol();
    }


    @Override
    public boolean validateTick(Tick tick) {

        // check these fields for all security-types
        if (tick.getBid() == null) {
            return false;
        } else if (tick.getAsk() == null) {
            return false;
        } else if (tick.getSettlement() == null) {
            return false;
        } else if (tick.getBidAskSpreadDouble() < 0) {
            int spreadTicks = -getSecurityFamily().getSpreadTicks(tick.getAsk(), tick.getBid());
            if (spreadTicks <= -2) {
                logger.warn("crossed spread: bid " + tick.getBid() + " ask " + tick.getAsk() + " for " + this);
                return false;
            } else {
                // no logging, as this happens often for Forex
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void initialize() {

        // initialize subscriptions before positions because the lazy loaded (= Proxy) Strategy
        // so subscriptions would also get the Proxy insead of the implementation
        long beforeSubscriptions = System.nanoTime();
        getSubscriptionsInitialized();
        long afterSubscriptions = System.nanoTime();

        // initialize positions
        long beforePositions = System.nanoTime();
        getPositionsInitialized();
        long afterPositions = System.nanoTime();

        // initialize underlying
        long beforeUnderlying = System.nanoTime();
        getUnderlyingInitialized();
        long afterUnderlying = System.nanoTime();

        // initialize securityFamily
        long beforeSecurityFamily = System.nanoTime();
        getSecurityFamilyInitialized();
        long afterSecurityFamily = System.nanoTime();

        MetricsUtil.account("Security.positions", (afterPositions - beforePositions));
        MetricsUtil.account("Security.subscriptions", (afterSubscriptions - beforeSubscriptions));
        MetricsUtil.account("Security.underlying", (afterUnderlying - beforeUnderlying));
        MetricsUtil.account("Security.securityFamily", (afterSecurityFamily - beforeSecurityFamily));
    }
}
