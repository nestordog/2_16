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
package ch.algotrader.service;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.entity.marketData.BarVO;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.entity.marketData.TickVO;
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
 */
public class MarketDataCacheImpl implements MarketDataCache, TickEventListener, BarEventListener {

    private static final Logger LOGGER = LogManager.getLogger(MarketDataCacheImpl.class);

    private final EngineManager engineManager;
    private final LookupService lookupService;
    private final Currency baseCurrency;
    private final int lookBackDays;

    private final ConcurrentMap<Long, MarketDataEventVO> lastMarketDataEventBySecurityId;
    private final ConcurrentMap<EnumSet<Currency>, Forex> forexMap;

    public MarketDataCacheImpl(
            final EngineManager engineManager,
            final LookupService lookupService,
            final Currency baseCurrency,
            final int lookBackDays) {

        Validate.notNull(engineManager, "EngineManager is null");
        Validate.notNull(lookupService, "LookupService is null");
        Validate.notNull(baseCurrency, "BaseCurrency is null");

        this.engineManager = engineManager;
        this.lookupService = lookupService;
        this.baseCurrency = baseCurrency;
        this.lookBackDays = lookBackDays;
        this.lastMarketDataEventBySecurityId = new ConcurrentHashMap<>();
        this.forexMap = new ConcurrentHashMap<>();
    }

    @Override
    public Map<Long, MarketDataEventVO> getCurrentMarketDataEvents() {

        return new HashMap<>(lastMarketDataEventBySecurityId);
    }

    @Override
    public MarketDataEventVO getCurrentMarketDataEvent(long securityId) {

        MarketDataEventVO last = lastMarketDataEventBySecurityId.get(securityId);
        if (last != null) {
            return last;
        }
        Tick entity = this.lookupService.getLastTick(securityId, this.engineManager.getCurrentEPTime(), this.lookBackDays);
        if (entity != null) {
            onMarketDataEvent(Tick.Converter.INSTANCE.convert(entity));
            // The market data may have changed in the meantime. Get the latest value.
            return lastMarketDataEventBySecurityId.get(securityId);
        }
        return null;
    }

    @Override
    public BigDecimal getCurrentValue(long securityId) {

        return getCurrentMarketDataEvent(securityId).getCurrentValue();
    }

    @Override
    public double getCurrentValueDouble(long securityId) {

        return getCurrentMarketDataEvent(securityId).getCurrentValueDouble();
    }

    @Override
    public double getForexRate(Currency baseCurrency, Currency transactionCurrency) {

        Validate.notNull(baseCurrency, "Base currency is null");
        Validate.notNull(transactionCurrency, "Transaction currency is null");

        if (baseCurrency.equals(transactionCurrency)) {
            return 1.0;
        }

        final EnumSet<Currency> key = EnumSet.of(baseCurrency, transactionCurrency);
        Forex forex = this.forexMap.get(key);
        if (forex == null) {
            forex = this.lookupService.getForex(baseCurrency, transactionCurrency);
            if (forex == null) {
                throw new ForexAvailabilityException("Forex does not exist: " + baseCurrency + "." + transactionCurrency);
            }
            this.forexMap.put(key, forex);//we may replace an existing forex entry due to racing but that's ok
        }

        final MarketDataEventVO marketDataEvent = getCurrentMarketDataEvent(forex.getId());

        if (marketDataEvent == null) {
            throw new ForexAvailabilityException("No exchange rate available for " + baseCurrency + "." + transactionCurrency);
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

        return getForexRate(baseCurrency, this.baseCurrency);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRate(long securityId, Currency transactionCurrency) {

        final Security security = this.lookupService.getSecurityInclUnderlyingFamilyAndExchange(securityId);

        return getForexRate(security, transactionCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRate(Security security, Currency transactionCurrency) {
        Validate.notNull(security, "Security is null");

        return getForexRate(security.getSecurityFamily().getCurrency(), transactionCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRateBase(long securityId) {

        return getForexRate(securityId, this.baseCurrency);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getForexRateBase(Security security) {

        return getForexRate(security, this.baseCurrency);
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
    public void flush(long securityId) {
        lastMarketDataEventBySecurityId.remove(securityId);
    }

    private void onMarketDataEvent(MarketDataEventVO event) {

        if (event.getDateTime() == null) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Market data event with missing timestamp: {}", event);
            }
            return;
        }
        lastMarketDataEventBySecurityId.merge(event.getSecurityId(), event, (lastEvent, newEvent) -> {
            if (lastEvent == null) {
                return newEvent;
            } else {
                return newEvent.getDateTime().compareTo(lastEvent.getDateTime()) >= 0 ? newEvent : lastEvent;
            }
        });
    }

    @Override
    public void onTick(TickVO tick) {
        onMarketDataEvent(tick);
    }

    @Override
    public void onBar(BarVO bar) {
        onMarketDataEvent(bar);
    }
}
