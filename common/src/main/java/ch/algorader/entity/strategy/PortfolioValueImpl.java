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
package ch.algorader.entity.strategy;

import java.text.SimpleDateFormat;

import ch.algorader.util.RoundUtil;

import com.algoTrader.entity.strategy.PortfolioValue;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioValueImpl extends PortfolioValue {

    private static final long serialVersionUID = -3646704287725745092L;
    private static SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy hh:mm:ss");

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

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();

        buffer.append(format.format(getDateTime()));
        buffer.append(",");
        buffer.append(getStrategy());
        buffer.append(",netLiqValue=");
        buffer.append(getNetLiqValue());
        buffer.append(buffer.append(",securitiesCurrentValue="));
        buffer.append(getSecuritiesCurrentValue());
        buffer.append(",cashBalance=");
        buffer.append(getCashBalance());
        buffer.append(",maintenanceMargin=");
        buffer.append(getMaintenanceMargin());
        buffer.append(",leverage=");
        buffer.append(RoundUtil.getBigDecimal(getLeverage()));
        buffer.append(",allocation=");
        buffer.append(RoundUtil.getBigDecimal(getAllocation()));

        if (getCashFlow() != null) {
            buffer.append(",cashFlow=");
            buffer.append(getCashFlow());
        }

        return buffer.toString();
    }
}
