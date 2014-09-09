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
package ch.algotrader.service;

import java.util.Date;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface CalendarService {

    /**
     * Gets the current trading date of the specified exchange. This represents the date when
     * the particular exchange opened for the last time before the specified dateTime
     */
    public Date getCurrentTradingDate(int exchangeId, Date dateTime);

    /**
     * Gets the current trading date of the specified exchange. This represents the date when
     * the particular exchange opened for the last time before the current time
     */
    public Date getCurrentTradingDate(int exchangeId);

    /**
     * returns true, if the exchange is currently open at the specified dateTime, taking into consideration the different
     * trading hours as well as holidays, early opens and early closes
     */
    public boolean isOpen(int exchangeId, Date dateTime);

    /**
     * returns true, if the exchange is open at the current dateTime, taking into consideration the different
     * trading hours as well as holidays, early opens and early closes
     */
    public boolean isOpen(int exchangeId);

    /**
     * returns true if the exchange is open on the specified date
     */
    public boolean isTradingDay(int exchangeId, Date date);

    /**
     * returns true if the exchange is open on the current date
     */
    public boolean isTradingDay(int exchangeId);

    /**
     * Gets the time the exchange opens on a particular date or null if the exchange is closed on that day
     */
    public Date getOpenTime(int exchangeId, Date date);

    /**
     * Gets the time the exchange opens on the current date or null if the exchange is closed on the current date
     */
    public Date getOpenTime(int exchangeId);

    /**
     * Gets the time the exchange closes on a particular date or null if the exchange is closed on that day
     */
    public Date getCloseTime(int exchangeId, Date date);

    /**
     * Gets the time the exchange closes on the current date or null if the exchange is closed on the current date
     */
    public Date getCloseTime(int exchangeId);

    /**
     * Gets the time the exchange opens the next time after the specified dateTime
     */
    public Date getNextOpenTime(int exchangeId, Date dateTime);

    /**
     * Gets the time the exchange opens the next time after the current dateTime
     */
    public Date getNextOpenTime(int exchangeId);

    /**
     * Gets the time the exchange closes the next time after the specified dateTime
     */
    public Date getNextCloseTime(int exchangeId, Date dateTime);

    /**
     * Gets the time the exchange closes the next time after the current dateTime
     */
    public Date getNextCloseTime(int exchangeId);

}
