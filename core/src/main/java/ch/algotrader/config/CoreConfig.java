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
            @ConfigName("misc.defaultFeedType") String defaultFeedType,
            @ConfigName("misc.defaultOrderPreference") String defaultOrderPreference,
            @ConfigName("fx.futureHedgeEnabled") boolean fxFutureHedgeEnabled,
            @ConfigName("fx.futureHedgeMinTimeToExpiration") int fxFutureHedgeMinTimeToExpiration,
            @ConfigName("fx.hedgeMinAmount") int fxHedgeMinAmount,
            @ConfigName("fx.hedgeBatchSize") int fxHedgeBatchSize,
            @ConfigName("fx.hedgeOrderPreference") String fxHedgeOrderPreference,
            @ConfigName("delta.hedgeMinTimeToExpiration") int deltaHedgeMinTimeToExpiration,
            @ConfigName("delta.hedgeOrderPreference") String deltaHedgeOrderPreference,
            @ConfigName("misc.positionCheckDisabled") boolean positionCheckDisabled) {
        this.simulateOptions = simulateOptions;
        this.simulateFuturesByUnderlying = simulateFuturesByUnderlying;
        this.simulateFuturesByGenericFutures = simulateFuturesByGenericFutures;
        this.transactionDisplayCount = transactionDisplayCount;
        this.intervalDays = intervalDays;
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
        return this.simulateOptions;
    }

    public boolean isSimulateFuturesByUnderlying() {
        return this.simulateFuturesByUnderlying;
    }

    public boolean isSimulateFuturesByGenericFutures() {
        return this.simulateFuturesByGenericFutures;
    }

    public int getTransactionDisplayCount() {
        return this.transactionDisplayCount;
    }

    public int getIntervalDays() {
        return this.intervalDays;
    }

    public String getDefaultFeedType() {
        return this.defaultFeedType;
    }

    public String getDefaultOrderPreference() {
        return this.defaultOrderPreference;
    }

    public boolean isFxFutureHedgeEnabled() {
        return this.fxFutureHedgeEnabled;
    }

    public int getFxFutureHedgeMinTimeToExpiration() {
        return this.fxFutureHedgeMinTimeToExpiration;
    }

    public int getFxHedgeMinAmount() {
        return this.fxHedgeMinAmount;
    }

    public int getFxHedgeBatchSize() {
        return this.fxHedgeBatchSize;
    }

    public String getFxHedgeOrderPreference() {
        return this.fxHedgeOrderPreference;
    }

    public int getDeltaHedgeMinTimeToExpiration() {
        return this.deltaHedgeMinTimeToExpiration;
    }

    public String getDeltaHedgeOrderPreference() {
        return this.deltaHedgeOrderPreference;
    }

    public boolean isPositionCheckDisabled() {
        return this.positionCheckDisabled;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("[");
        sb.append("simulateOptions=").append(this.simulateOptions);
        sb.append(", simulateFuturesByUnderlying=").append(this.simulateFuturesByUnderlying);
        sb.append(", simulateFuturesByGenericFutures=").append(this.simulateFuturesByGenericFutures);
        sb.append(", transactionDisplayCount=").append(this.transactionDisplayCount);
        sb.append(", intervalDays=").append(this.intervalDays);
        sb.append(", defaultFeedType=").append(this.defaultFeedType);
        sb.append(", defaultOrderPreference=").append(this.defaultOrderPreference);
        sb.append(", fxFutureHedgeEnabled=").append(this.fxFutureHedgeEnabled);
        sb.append(", fxFutureHedgeMinTimeToExpiration=").append(this.fxFutureHedgeMinTimeToExpiration);
        sb.append(", fxHedgeMinAmount=").append(this.fxHedgeMinAmount);
        sb.append(", fxHedgeBatchSize=").append(this.fxHedgeBatchSize);
        sb.append(", fxHedgeOrderPreference=").append(this.fxHedgeOrderPreference);
        sb.append(", deltaHedgeMinTimeToExpiration=").append(this.deltaHedgeMinTimeToExpiration);
        sb.append(", deltaHedgeOrderPreference=").append(this.deltaHedgeOrderPreference);
        sb.append(", positionCheckDisabled=").append(this.positionCheckDisabled);
        sb.append(']');
        return sb.toString();
    }

}
