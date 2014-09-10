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

package ch.algotrader.config;

import java.math.BigDecimal;

import ch.algotrader.enumeration.FeedType;

/**
 * Algotrader core configuration object.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class CoreConfig {

    private final boolean simulateOptions;
    private final boolean simulateFuturesByUnderlying;
    private final boolean simulateFuturesByGenericFutures;
    private final int transactionDisplayCount;
    private final int intervalDays;
    private final BigDecimal rebalanceMinAmount;
    private final FeedType defaultFeedType;
    private final boolean fxFutureHedgeEnabled;
    private final int fxFutureHedgeMinTimeToExpiration;
    private final int fxHedgeMinAmount;
    private final int fxHedgeBatchSize;
    private final int deltaHedgeMinTimeToExpiration;

    public CoreConfig(
            @ConfigName("statement.simulateOptions") boolean simulateOptions,
            @ConfigName("statement.simulateFuturesByUnderlying") boolean simulateFuturesByUnderlying,
            @ConfigName("statement.simulateFuturesByGenericFutures") boolean simulateFuturesByGenericFutures,
            @ConfigName("misc.transactionDisplayCount") int transactionDisplayCount,
            @ConfigName("misc.intervalDays") int intervalDays,
            @ConfigName("misc.rebalanceMinAmount") BigDecimal rebalanceMinAmount,
            @ConfigName("misc.defaultFeedType") FeedType defaultFeedType,
            @ConfigName("fx.futureHedgeEnabled") boolean fxFutureHedgeEnabled,
            @ConfigName("fx.futureHedgeMinTimeToExpiration") int fxFutureHedgeMinTimeToExpiration,
            @ConfigName("fx.hedgeMinAmount") int fxHedgeMinAmount,
            @ConfigName("fx.hedgeBatchSize") int fxHedgeBatchSize,
            @ConfigName("delta.hedgeMinTimeToExpiration") int deltaHedgeMinTimeToExpiration) {
        this.simulateOptions = simulateOptions;
        this.simulateFuturesByUnderlying = simulateFuturesByUnderlying;
        this.simulateFuturesByGenericFutures = simulateFuturesByGenericFutures;
        this.transactionDisplayCount = transactionDisplayCount;
        this.intervalDays = intervalDays;
        this.rebalanceMinAmount = rebalanceMinAmount;
        this.defaultFeedType = defaultFeedType;
        this.fxFutureHedgeEnabled = fxFutureHedgeEnabled;
        this.fxFutureHedgeMinTimeToExpiration = fxFutureHedgeMinTimeToExpiration;
        this.fxHedgeMinAmount = fxHedgeMinAmount;
        this.fxHedgeBatchSize = fxHedgeBatchSize;
        this.deltaHedgeMinTimeToExpiration = deltaHedgeMinTimeToExpiration;
    }

    public boolean isSimulateOptions() {
        return simulateOptions;
    }

    public boolean isSimulateFuturesByUnderlying() {
        return simulateFuturesByUnderlying;
    }

    public boolean isSimulateFuturesByGenericFutures() {
        return simulateFuturesByGenericFutures;
    }

    public int getTransactionDisplayCount() {
        return transactionDisplayCount;
    }

    public int getIntervalDays() {
        return intervalDays;
    }

    public BigDecimal getRebalanceMinAmount() {
        return rebalanceMinAmount;
    }

    public FeedType getDefaultFeedType() {
        return defaultFeedType;
    }

    public boolean isFxFutureHedgeEnabled() {
        return fxFutureHedgeEnabled;
    }

    public int getFxFutureHedgeMinTimeToExpiration() {
        return fxFutureHedgeMinTimeToExpiration;
    }

    public int getFxHedgeMinAmount() {
        return fxHedgeMinAmount;
    }

    public int getFxHedgeBatchSize() {
        return fxHedgeBatchSize;
    }

    public int getDeltaHedgeMinTimeToExpiration() {
        return deltaHedgeMinTimeToExpiration;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("simulateOptions=").append(simulateOptions);
        sb.append(", simulateFuturesByUnderlying=").append(simulateFuturesByUnderlying);
        sb.append(", simulateFuturesByGenericFutures=").append(simulateFuturesByGenericFutures);
        sb.append(", transactionDisplayCount=").append(transactionDisplayCount);
        sb.append(", intervalDays=").append(intervalDays);
        sb.append(", rebalanceMinAmount=").append(rebalanceMinAmount);
        sb.append(", defaultFeedType=").append(defaultFeedType);
        sb.append(", fxFutureHedgeEnabled=").append(fxFutureHedgeEnabled);
        sb.append(", fxFutureHedgeMinTimeToExpiration=").append(fxFutureHedgeMinTimeToExpiration);
        sb.append(", fxHedgeMinAmount=").append(fxHedgeMinAmount);
        sb.append(", fxHedgeBatchSize=").append(fxHedgeBatchSize);
        sb.append(", deltaHedgeMinTimeToExpiration=").append(deltaHedgeMinTimeToExpiration);
        sb.append(']');
        return sb.toString();
    }

}
