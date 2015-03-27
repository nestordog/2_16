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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.collections15.map.MultiKeyMap;
import org.apache.commons.lang.Validate;
import org.apache.log4j.Logger;

import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.MarketDataEvent;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.esper.EngineManager;
import ch.algotrader.event.listener.BarEventListener;
import ch.algotrader.event.listener.TickEventListener;

/**
 * Lookup for market data cached locally in the current VM.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class LocalLookupServiceImpl implements LocalLookupService, TickEventListener, BarEventListener {

    private static final Logger LOG = Logger.getLogger(LocalLookupServiceImpl.class);

    private final CommonConfig commonConfig;
    private final EngineManager engineManager;
    private final LookupService lookupService;

    private final ConcurrentMap<Integer, MarketDataEvent> lastMarketDataEventBySecurityId;
    private final MultiKeyMap<Currency, Forex> forexMap;

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
        this.lastMarketDataEventBySecurityId = new ConcurrentHashMap<>();
        this.forexMap = new MultiKeyMap<>();//TODO thread safe?!?
    }

    @Override
    public MarketDataEvent getCurrentMarketDataEvent(int securityId) {

        final MarketDataEvent last = lastMarketDataEventBySecurityId.get(securityId);
        if (last != null) {
            return last;
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

        Security security = this.lookupService.getSecurityInclFamilyAndUnderlying(securityId);
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

    @Override
    public boolean hasCurrentMarketDataEvents() {
        return !lastMarketDataEventBySecurityId.isEmpty();
    }

    /**
     * Clears cached market data events for all securities.
     */
    public void flush() {
        lastMarketDataEventBySecurityId.clear();
    }

    /**
     * Clears the cached market data event for the specified security.
     * @param securityId the ID of the security whose cached market data event to clear
     */
    public void flush(int securityId) {
        lastMarketDataEventBySecurityId.remove(securityId);
    }

    private void onMarketDataEvent(MarketDataEvent event) {
        final int maxTries = 3;
        final Integer securityId = event.getSecurity().getId();
        MarketDataEvent old = lastMarketDataEventBySecurityId.get(securityId);
        //only accept event if it is newer than our current last
        int cnt = 0;
        while (old == null || old != event & old.getDateTime().compareTo(event.getDateTime()) <= 0) {
            if (old == null && null != lastMarketDataEventBySecurityId.putIfAbsent(securityId, event)) {
                return;
            } else if (old != null && lastMarketDataEventBySecurityId.replace(securityId, old, event)) {
                return;
            }
            //there must have been a concurrent update for the same security
            cnt++;
            if (cnt >= maxTries) {
                LOG.warn("ignoring market data event due to concurrent updates, giving up after " + cnt + " tries: " + event);
                return;
            }
            //let's try again
            old = lastMarketDataEventBySecurityId.get(securityId);
        }
        //else: event is older than our current last
    }

    @Override
    public void onTick(Tick tick) {
        onMarketDataEvent(tick);
    }

    @Override
    public void onBar(Bar bar) {
        onMarketDataEvent(bar);
    }
}
