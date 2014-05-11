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
package ch.algotrader.future;

import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.util.DateUtil;

/**
 * Utility class containing static methods around {@link Future Futures}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FutureUtil {

    /**
     * Gets the fair-price of a {@link Future} based on the price of the {@code underlyingSpot}.
     * {@code duration}, {@code intrest} and {@code dividend} are retrieved from the {@link FutureFamily}.
     */
    public static double getFuturePrice(Future future, double underlyingSpot) {

        FutureFamily family = (FutureFamily) future.getSecurityFamily();

        double years = (future.getExpiration().getTime() - DateUtil.getCurrentEPTime().getTime()) / (double) Duration.YEAR_1.getValue();

        return getFuturePrice(underlyingSpot, years, family.getIntrest(), family.getDividend());
    }

    /**
     * Gets the fair-price of a {@link Future}.
     */
    public static double getFuturePrice(double underlyingSpot, double years, double intrest, double dividend) {

        return underlyingSpot + (underlyingSpot * intrest - dividend) * years;
    }

    /**
     * Gets the current maintenace margin of a {@link Future} as defined by the {@link FutureFamily}.
     */
    public static double getMaintenanceMargin(Future future) {

        FutureFamily family = (FutureFamily) future.getSecurityFamily();
        return family.getMarginParameter();
    }
}
