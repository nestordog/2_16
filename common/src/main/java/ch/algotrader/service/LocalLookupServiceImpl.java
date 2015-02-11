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

import java.math.BigDecimal;
import java.util.List;

import org.apache.commons.collections15.map.MultiKeyMap;
import org.apache.commons.lang.Validate;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.util.collection.CollectionUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LocalLookupServiceImpl implements LocalLookupService {

    private final CommonConfig commonConfig;
    private final EngineManager engineManager;
    private final LookupService lookupService;

    private final MultiKeyMap<Currency, Forex> forexMap = new MultiKeyMap<Currency, Forex>();

    public LocalLookupServiceImpl(
            final CommonConfig commonConfig,
            final EngineManager engineManager,
            final LookupService lookupService) {

        Validate.notNull(commonConfig, "CommonConfig is null");
        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(lookupService, "LookupService is null");

        this.commonConfig = commonConfig;
        this.engineManager = engineManager;
        this.lookupService = lookupService;
    }

    @Override
    @SuppressWarnings({ "unchecked" })
    public MarketDataEvent getCurrentMarketDataEvent(int securityId) {

        String startedStrategyName = this.commonConfig.getStartedStrategyName();
        if (this.engineManager.hasEngine(startedStrategyName)) {

            Engine engine = this.engineManager.getEngine(startedStrategyName);
            if (engine.isDeployed("MARKET_DATA_WINDOW")) {

                List<MarketDataEvent> events = engine.executeQuery("select marketDataEvent.* from MarketDataWindow where securityId = " + securityId + " order by marketDataEvent.dateTime desc");

                // might have multiple events of different feed types
                if (events.size() > 0) {
                    return CollectionUtil.getFirstElement(events);
                }

            }
        }

        return this.lookupService.getLastTick(securityId, this.engineManager.getCurrentEPTime());
    }

    @Override
    public BigDecimal getCurrentValue(int securityId) {

        return getCurrentMarketDataEvent(securityId).getCurrentValue();
    }

    @Override
    public double getCurrentValueDouble(int securityId) {

        return getCurrentMarketDataEvent(securityId).getCurrentValueDouble();
    }

    @Override
    public double getForexRate(Currency baseCurrency, Currency transactionCurrency) {

        Validate.notNull(baseCurrency, "Base currency is null");
        Validate.notNull(transactionCurrency, "Transaction currency is null");

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        Forex forex = this.forexMap.get(baseCurrency, transactionCurrency);
        if (forex == null) {
            forex = this.lookupService.getForex(baseCurrency, transactionCurrency);
            this.forexMap.put(baseCurrency, transactionCurrency, forex);
        }

        MarketDataEvent marketDataEvent = getCurrentMarketDataEvent(forex.getId());

        if (marketDataEvent == null) {
            throw new IllegalStateException("Cannot get exchangeRate for " + baseCurrency + "." + transactionCurrency + " because no marketDataEvent is available");
        }

        if (forex.getBaseCurrency().equals(baseCurrency)) {
            // expected case
            return marketDataEvent.getCurrentValueDouble();
        } else {
            // reverse case
            return 1.0 / marketDataEvent.getCurrentValueDouble();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRateBase(final Currency baseCurrency) {

        Validate.notNull(baseCurrency, "Base currency is null");

        return getForexRate(baseCurrency, this.commonConfig.getPortfolioBaseCurrency());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRate(int securityId, Currency transactionCurrency) {

        Security security = this.lookupService.getSecurity(securityId);
        Validate.notNull(security, "Security is null");

        return getForexRate(security.getSecurityFamily().getCurrency(), transactionCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRateBase(int securityId) {

        return getForexRate(securityId, this.commonConfig.getPortfolioBaseCurrency());
    }
}
