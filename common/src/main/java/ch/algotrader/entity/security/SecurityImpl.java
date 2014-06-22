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
package ch.algotrader.entity.security;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;

import com.espertech.esper.event.WrapperEventBean;
import com.espertech.esper.event.bean.BeanEventBean;

import ch.algotrader.ServiceLocator;
import ch.algotrader.config.ConfigLocator;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.ObjectUtil;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public abstract class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    private static Logger logger = MyLogger.getLogger(SecurityImpl.class.getName());

    private static @Value("#{T(ch.algotrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private static @Value("${misc.initialMarginMarkup}") double initialMarginMarkup;
    private static @Value("${misc.validateCrossedSpread}") boolean validateCrossedSpread;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public MarketDataEvent getCurrentMarketDataEvent() {

        String startedStrategyName = ConfigLocator.instance().getCommonConfig().getStrategyName();
        if (EngineLocator.instance().hasEngine(startedStrategyName)) {
            List<Map> events = EngineLocator.instance().getEngine(startedStrategyName).getAllEvents("CURRENT_MARKET_DATA_EVENT");

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

            double contractSize = getSecurityFamily().getContractSize();
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

        // BId / ASK cannot be negative
        if (tick.getBid() != null && tick.getBid().doubleValue() < 0) {
            return false;
        } else if (tick.getAsk() != null && tick.getAsk().doubleValue() < 0) {
            return false;
        }

            // spread cannot be crossed
        if (validateCrossedSpread && tick.getBid() != null && tick.getAsk() != null && tick.getBidAskSpreadDouble() < 0) {
            logger.warn("crossed spread: bid " + tick.getBid() + " ask " + tick.getAsk() + " for " + this);
            return false;
        } else {
            return true;
        }
    }

    private transient boolean initialized = false;

    public boolean isInitialized() {

        return this.initialized;
    }

    @Override
    public void initialize() {

        if (!isInitialized()) {

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

            this.initialized = true;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof Security) {
            Security that = (Security) obj;
            return ObjectUtil.equals(this.getIsin(), that.getIsin()) &&
                        ObjectUtil.equals(this.getBbgid(), that.getBbgid()) &&
                        ObjectUtil.equals(this.getRic(), that.getRic()) &&
                        ObjectUtil.equals(this.getConid(), that.getConid()) &&
                        ObjectUtil.equals(this.getLmaxid(), that.getLmaxid()) &&
                        ObjectUtil.equals(this.getSymbol(), that.getSymbol());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(this.getIsin());
        hash = hash * 37 + ObjectUtil.hashCode(this.getBbgid());
        hash = hash * 37 + ObjectUtil.hashCode(this.getRic());
        hash = hash * 37 + ObjectUtil.hashCode(this.getConid());
        hash = hash * 37 + ObjectUtil.hashCode(this.getLmaxid());
        hash = hash * 37 + ObjectUtil.hashCode(this.getSymbol());
        return hash;
    }
}
