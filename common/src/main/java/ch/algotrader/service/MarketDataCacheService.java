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
import java.util.Map;

import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.security.Security;
import ch.algotrader.enumeration.Currency;

/**
 * Local market data cache.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface MarketDataCacheService {

    /**
     * Returns all last market events for all securities,
     */
    Map<Long, MarketDataEventVO> getCurrentMarketDataEvents();

    /**
     * Returns last market event of the specified security.
     */
    MarketDataEventVO getCurrentMarketDataEvent(long securityId);

    /**
     * Returns the current value of the specified security.
     */
    BigDecimal getCurrentValue(long securityId);

    /**
     * Gets the current Exchange Rate between the {@code baseCurrency} and {@code transactionCurrency}.
     */
    double getForexRate(Currency baseCurrency, Currency transactionCurrency);

    /**
     * Gets the current Exchange Rate between the {@code baseCurrency} and Portfolio Base Currency.
     */
    double getForexRateBase(Currency baseCurrency);

    /**
     * Gets the relevant exchange rate for the specified Security related to the specified {@link Currency}
     */
    double getForexRate(long securityId, Currency transactionCurrency);

    /**
     * Gets the relevant exchange rate for the specified Security related to the specified {@link Currency}
     */
    double getForexRate(Security security, Currency transactionCurrency);

    /**
     * Gets the relevant exchange rate for the specified Security related to the Portfolio Base Currency
     */
    double getForexRateBase(long securityId);

    /**
     * Gets the relevant exchange rate for the specified Security related to the Portfolio Base Currency
     */
    double getForexRateBase(Security security);

    /**
     * Returns true if any {@link ch.algotrader.entity.marketData.MarketDataEvent MarketDataEvent} has been received.
     */
    boolean hasCurrentMarketDataEvents();

    /**
     * Clears cached market data events for all securities.
     */
    void flush();

    /**
     * Clears the cached market data event for the specified security.
     * @param securityId the ID of the security whose cached market data event to clear
     */
    void flush(long securityId);

}
