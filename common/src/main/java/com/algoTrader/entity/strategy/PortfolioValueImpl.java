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
package com.algoTrader.entity.strategy;

import java.text.SimpleDateFormat;

import com.algoTrader.util.RoundUtil;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioValueImpl extends PortfolioValue {

    private static final long serialVersionUID = -3646704287725745092L;
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy hh:mm:ss");

    @Override
    public String toString() {

        //@formatter:off
        return format.format(getDateTime()) + " " + getStrategy() +
                " netLiqValue: " + getNetLiqValue() +
                " securitiesCurrentValue: " + getSecuritiesCurrentValue() +
                " cashBalance: " + getCashBalance() +
                " maintenanceMargin: " +getMaintenanceMargin() +
                " leverage: " + RoundUtil.getBigDecimal(getLeverage()) +
                " allocation: " + RoundUtil.getBigDecimal(getAllocation()) +
                ((getCashFlow() != null) ? " cashFlow: " + getCashFlow() : "");
        //@formatter:on
    }

    @Override
    public double getNetLiqValueDouble() {

        return getNetLiqValue().doubleValue();
    }

    @Override
    public double getSecuritiesCurrentValueDouble() {

        return getSecuritiesCurrentValue().doubleValue();
    }

    @Override
    public double getCashBalanceDouble() {

        return getCashBalance().doubleValue();
    }

    @Override
    public double getMaintenanceMarginDouble() {

        return getMaintenanceMargin().doubleValue();
    }
}
