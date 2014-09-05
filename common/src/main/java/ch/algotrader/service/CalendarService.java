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
     * returns true, if the exchange is currently open, taking into consideration the different
     * trading hours as well as holidays, early opens and early closes
     */
    public boolean isOpen(int exchangeId, Date dateTime);

    /**
     * returns true if the exchange is open on the specified date
     */
    public boolean isTradingDay(int exchangeId, Date date);

    public Date getOpenTime(int exchangeId, Date date);

    public Date getCloseTime(int exchangeId, Date date);

}
