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
package ch.algotrader.entity.strategy;

import java.util.Date;
import ch.algotrader.util.DateTimeUtil;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioValueImpl extends PortfolioValue {

    private static final long serialVersionUID = -3646704287725745092L;

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
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        Date dateTime = getDateTime();
        DateTimeUtil.formatLocalZone(dateTime.toInstant(), buffer);
        buffer.append(",");
        buffer.append(getStrategy());
        buffer.append(",netLiqValue=");
        buffer.append(getNetLiqValue());
        buffer.append(",securitiesCurrentValue=");
        buffer.append(getSecuritiesCurrentValue());
        buffer.append(",cashBalance=");
        buffer.append(getCashBalance());
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
