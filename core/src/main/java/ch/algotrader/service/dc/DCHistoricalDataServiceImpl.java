/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service.dc;

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.TimePeriod;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCHistoricalDataServiceImpl extends DCHistoricalDataServiceBase {

    private static final long serialVersionUID = -8246870605659050512L;

    @Override
    protected void handleInit() throws Exception {

    }

    @Override
    protected List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, Duration barSize, BarType barType) throws Exception {
        return null;
    }

}
