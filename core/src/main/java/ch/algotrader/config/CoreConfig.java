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

package ch.algotrader.config;

import java.math.BigDecimal;

/**
 * Algotrader core configuration object.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class CoreConfig {

    private final boolean simulateOptions;
    private final boolean simulateFuturesByUnderlying;
    private final boolean simulateFuturesByGenericFutures;
    private final int transactionDisplayCount;
    private final int intervalDays;
    private final BigDecimal rebalanceMinAmount;
    private final String defaultFeedType;
    private final String defaultOrderPreference;
    private final boolean fxFutureHedgeEnabled;
    private final int fxFutureHedgeMinTimeToExpiration;
    private final int fxHedgeMinAmount;
    private final int fxHedgeBatchSize;
    private final String fxHedgeOrderPreference;
    private final int deltaHedgeMinTimeToExpiration;
    private final String deltaHedgeOrderPreference;
    private final boolean positionCheckDisabled;

    public CoreConfig(
            @ConfigName("statement.simulateOptions") boolean simulateOptions,
            @ConfigName("statement.simulateFuturesByUnderlying") boolean simulateFuturesByUnderlying,
            @ConfigName("statement.simulateFuturesByGenericFutures") boolean simulateFuturesByGenericFutures,
            @ConfigName("misc.transactionDisplayCount") int transactionDisplayCount,
            @ConfigName("misc.intervalDays") int intervalDays,
            @ConfigName("misc.rebalanceMinAmount") BigDecimal rebalanceMinAmount,
            @ConfigName("misc.defaultFeedType") String defaultFeedType,
            @ConfigName("misc.defaultOrderPreference") String defaultOrderPreference,
            @ConfigName("fx.futureHedgeEnabled") boolean fxFutureHedgeEnabled,
            @ConfigName("fx.futureHedgeMinTimeToExpiration") int fxFutureHedgeMinTimeToExpiration,
            @ConfigName("fx.hedgeMinAmount") int fxHedgeMinAmount,
            @ConfigName("fx.hedgeBatchSize") int fxHedgeBatchSize,
            @ConfigName("fx.hedgeOrderPreference") String fxHedgeOrderPreference,
            @ConfigName("delta.hedgeMinTimeToExpiration") int deltaHedgeMinTimeToExpiration,
            @ConfigName("delta.hedgeOrderPreference") String deltaHedgeOrderPreference,
            @ConfigName("persistence.positionCheckDisabled") boolean positionCheckDisabled) {
        this.simulateOptions = simulateOptions;
        this.simulateFuturesByUnderlying = simulateFuturesByUnderlying;
        this.simulateFuturesByGenericFutures = simulateFuturesByGenericFutures;
        this.transactionDisplayCount = transactionDisplayCount;
        this.intervalDays = intervalDays;
        this.rebalanceMinAmount = rebalanceMinAmount;
        this.defaultFeedType = defaultFeedType;
        this.defaultOrderPreference = defaultOrderPreference;
        this.fxFutureHedgeEnabled = fxFutureHedgeEnabled;
        this.fxFutureHedgeMinTimeToExpiration = fxFutureHedgeMinTimeToExpiration;
        this.fxHedgeMinAmount = fxHedgeMinAmount;
        this.fxHedgeBatchSize = fxHedgeBatchSize;
        this.fxHedgeOrderPreference = fxHedgeOrderPreference;
        this.deltaHedgeMinTimeToExpiration = deltaHedgeMinTimeToExpiration;
        this.deltaHedgeOrderPreference = deltaHedgeOrderPreference;
        this.positionCheckDisabled = positionCheckDisabled;
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

    public String getDefaultFeedType() {
        return defaultFeedType;
    }

    public String getDefaultOrderPreference() {
        return defaultOrderPreference;
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

    public String getFxHedgeOrderPreference() {
        return fxHedgeOrderPreference;
    }

    public int getDeltaHedgeMinTimeToExpiration() {
        return deltaHedgeMinTimeToExpiration;
    }

    public String getDeltaHedgeOrderPreference() {
        return deltaHedgeOrderPreference;
    }

    public boolean isPositionCheckDisabled() {
        return positionCheckDisabled;
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
        sb.append(", defaultOrderPreference=").append(defaultOrderPreference);
        sb.append(", fxFutureHedgeEnabled=").append(fxFutureHedgeEnabled);
        sb.append(", fxFutureHedgeMinTimeToExpiration=").append(fxFutureHedgeMinTimeToExpiration);
        sb.append(", fxHedgeMinAmount=").append(fxHedgeMinAmount);
        sb.append(", fxHedgeBatchSize=").append(fxHedgeBatchSize);
        sb.append(", fxHedgeOrderPreference=").append(fxHedgeOrderPreference);
        sb.append(", deltaHedgeMinTimeToExpiration=").append(deltaHedgeMinTimeToExpiration);
        sb.append(", deltaHedgeOrderPreference=").append(deltaHedgeOrderPreference);
        sb.append(", positionCheckDisabled=").append(positionCheckDisabled);
        sb.append(']');
        return sb.toString();
    }

}
