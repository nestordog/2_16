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
package ch.algotrader.entity.security;

import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.ExpirationType;

/**
 * Represents a Family of Securities that have an expiration date
 */
public interface ExpirableFamilyI {

    /**
     * The Type of Expiration Logic utilized by Securities of this ExpirableFamilyI.
     * @return ExpirationType
     */
    public ExpirationType getExpirationType();

    /**
     * The Type of Expiration Logic utilized by Securities of this ExpirableFamilyI.
     * @param expirationType ExpirationType
     */
    public void setExpirationType(ExpirationType expirationType);

    /**
     * The Duration between two Instances of this ExpirableFamilyI. (e.g. 3 months for EUR.USD Forex
     * Futures)
     * @return Duration
     */
    public Duration getExpirationDistance();

    /**
     * The Duration between two Instances of this ExpirableFamilyI. (e.g. 3 months for EUR.USD Forex
     * Futures)
     * @param expirationDistance Duration
     */
    public void setExpirationDistance(Duration expirationDistance);

}