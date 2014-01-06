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
package ch.algotrader.entity.strategy;

import java.text.SimpleDateFormat;

import ch.algotrader.util.ObjectUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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


    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof PortfolioValue) {
            PortfolioValue that = (PortfolioValue) obj;
            return ObjectUtil.equalsNonNull(this.getStrategy(), that.getStrategy()) &&
                    ObjectUtil.equalsNonNull(this.getDateTime(), that.getDateTime());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + ObjectUtil.hashCode(getStrategy());
        hash = hash * 37 + ObjectUtil.hashCode(getDateTime());
        return hash;
    }
}
