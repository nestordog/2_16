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

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.enumeration.Duration;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface ServerLookupService {

    /**
     * Gets the securityId by the specified securityString, by checking fields
     * in the following order:<br>
     * <ul>
     * <li>symbol</li>
     * <li>isin</li>
     * <li>bbgid</li>
     * <li>ric</li>
     * <li>conid</li>
     * <li>id</li>
     * </ul>
     */
    public long getSecurityIdBySecurityString(String securityString);

    /**
     * Gets all Subscriptions by the defined {@code strategyName}. If corresponding Securities are
     * Combinations, all Components will be initialized as well. In additional all Properties are initialized
     */
    public List<Subscription> getSubscriptionsByStrategyInclComponentsAndProps(String strategyName);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public List<Tick> getSubscribedTicksByTimePeriod(Date startDate, Date endDate);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public Tick getFirstSubscribedTick();

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public List<Bar> getSubscribedBarsByTimePeriodAndBarSize(Date startDate, Date endDate, Duration barSize);

    /**
     * Gets all Ticks for Securities that are subscribed by any Strategy between {@code minDate} and
     * {@code maxDate}
     */
    public Bar getFirstSubscribedBarByBarSize(Duration barSize);

    /**
     * initialize all security Strings for subscribed Securities
     */
    public void initSecurityStrings();

}
