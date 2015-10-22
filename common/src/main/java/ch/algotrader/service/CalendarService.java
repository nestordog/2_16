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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface CalendarService {

    /**
     * Gets the current trading date of the specified exchange. This represents the date when
     * the particular exchange opened for the last time before the specified dateTime
     */
    public Date getCurrentTradingDate(long exchangeId, Date dateTime);

    /**
     * Gets the current trading date of the specified exchange. This represents the date when
     * the particular exchange opened for the last time before the current time
     */
    public Date getCurrentTradingDate(long exchangeId);

    /**
     * returns true, if the exchange is currently open at the specified dateTime, taking into consideration the different
     * trading hours as well as holidays, early opens and early closes
     */
    public boolean isOpen(long exchangeId, Date dateTime);

    /**
     * returns true, if the exchange is open at the current dateTime, taking into consideration the different
     * trading hours as well as holidays, early opens and early closes
     */
    public boolean isOpen(long exchangeId);

    /**
     * returns true if the exchange is open on the specified date
     */
    public boolean isTradingDay(long exchangeId, Date date);

    /**
     * returns true if the exchange is open on the current date
     */
    public boolean isTradingDay(long exchangeId);

    /**
     * Gets the time the exchange opens on a particular date or null if the exchange is closed on that day
     */
    public Date getOpenTime(long exchangeId, Date date);

    /**
     * Gets the time the exchange opens on the current date or null if the exchange is closed on the current date
     */
    public Date getOpenTime(long exchangeId);

    /**
     * Gets the time the exchange closes on a particular date or null if the exchange is closed on that day
     */
    public Date getCloseTime(long exchangeId, Date date);

    /**
     * Gets the time the exchange closes on the current date or null if the exchange is closed on the current date
     */
    public Date getCloseTime(long exchangeId);

    /**
     * Gets the time the exchange opens the next time after the specified dateTime
     */
    public Date getNextOpenTime(long exchangeId, Date dateTime);

    /**
     * Gets the time the exchange opens the next time after the current dateTime
     */
    public Date getNextOpenTime(long exchangeId);

    /**
     * Gets the time the exchange closes the next time after the specified dateTime
     */
    public Date getNextCloseTime(long exchangeId, Date dateTime);

    /**
     * Gets the time the exchange closes the next time after the current dateTime
     */
    public Date getNextCloseTime(long exchangeId);

}
