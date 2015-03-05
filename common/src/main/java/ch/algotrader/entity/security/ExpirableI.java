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

import java.util.Date;

/**
 * Represents a Security with an expiration date
 */
public interface ExpirableI {

    public Date getExpiration();

    public void setExpiration(Date expiration);

    /**
     * Gets the time-to-expiration in milliseconds from the specified {@code dateTime}
     * @param dateTime
     * @return long
     */
    public long getTimeToExpiration(Date dateTime);

    /**
     * Gets the Duration of this ExpirableI from the specified {@code dateTime). A Duration of 1
     * means that this is the next Object in the Chain to expire.
     * @param dateTime
     * @return int
     */
    public int getDuration(Date dateTime);

}