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
 * Factory for Algotrader core platform configuration objects.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public final class CoreConfigBuilder {

    private boolean simulateOptions;
    private boolean simulateFuturesByUnderlying;
    private boolean simulateFuturesByGenericFutures;
    private int transactionDisplayCount;
    private int intervalDays;
    private BigDecimal rebalanceMinAmount;
    private FeedType defaultFeedType;
    private boolean fxFutureHedgeEnabled;
    private int fxFutureHedgeMinTimeToExpiration;
    private int fxHedgeMinAmount;
    private int fxHedgeBatchSize;
    private int deltaHedgeMinTimeToExpiration;
    private boolean positionCheckDisabled;

    CoreConfigBuilder() {
        this.rebalanceMinAmount = new BigDecimal("1000");
        this.defaultFeedType = FeedType.IB;
    }

    public static CoreConfigBuilder create() {
        return new CoreConfigBuilder();
    }

    public CoreConfigBuilder setSimulateOptions(boolean simulateOptions) {
        this.simulateOptions = simulateOptions;
        return this;
    }

    public CoreConfigBuilder setSimulateFuturesByUnderlying(boolean simulateFuturesByUnderlying) {
        this.simulateFuturesByUnderlying = simulateFuturesByUnderlying;
        return this;
    }

    public CoreConfigBuilder setSimulateFuturesByGenericFutures(boolean simulateFuturesByGenericFutures) {
        this.simulateFuturesByGenericFutures = simulateFuturesByGenericFutures;
        return this;
    }

    public CoreConfigBuilder setTransactionDisplayCount(int transactionDisplayCount) {
        this.transactionDisplayCount = transactionDisplayCount;
        return this;
    }

    public CoreConfigBuilder setIntervalDays(int intervalDays) {
        this.intervalDays = intervalDays;
        return this;
    }

    public CoreConfigBuilder setRebalanceMinAmount(BigDecimal rebalanceMinAmount) {
        this.rebalanceMinAmount = rebalanceMinAmount;
        return this;
    }

    public CoreConfigBuilder setDefaultFeedType(FeedType defaultFeedType) {
        this.defaultFeedType = defaultFeedType;
        return this;
    }

    public CoreConfigBuilder setFxFutureHedgeEnabled(boolean fxFutureHedgeEnabled) {
        this.fxFutureHedgeEnabled = fxFutureHedgeEnabled;
        return this;
    }

    public CoreConfigBuilder setFxFutureHedgeMinTimeToExpiration(int fxFutureHedgeMinTimeToExpiration) {
        this.fxFutureHedgeMinTimeToExpiration = fxFutureHedgeMinTimeToExpiration;
        return this;
    }

    public CoreConfigBuilder setFxHedgeMinAmount(int fxHedgeMinAmount) {
        this.fxHedgeMinAmount = fxHedgeMinAmount;
        return this;
    }

    public CoreConfigBuilder setFxHedgeBatchSize(int fxHedgeBatchSize) {
        this.fxHedgeBatchSize = fxHedgeBatchSize;
        return this;
    }

    public CoreConfigBuilder setDeltaHedgeMinTimeToExpiration(int deltaHedgeMinTimeToExpiration) {
        this.deltaHedgeMinTimeToExpiration = deltaHedgeMinTimeToExpiration;
        return this;
    }

    public CoreConfigBuilder setPositionCheckDisabled(final boolean positionCheckDisabled) {
        this.positionCheckDisabled = positionCheckDisabled;
        return this;
    }

    public CoreConfig build() {
        return new CoreConfig(simulateOptions, simulateFuturesByUnderlying, simulateFuturesByGenericFutures, transactionDisplayCount,
                intervalDays, rebalanceMinAmount, defaultFeedType,
                fxFutureHedgeEnabled, fxFutureHedgeMinTimeToExpiration, fxHedgeMinAmount, fxHedgeBatchSize,
                deltaHedgeMinTimeToExpiration, positionCheckDisabled);
    }

}
