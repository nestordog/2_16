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
package ch.algotrader.util;

/**
 * Provides utility methods to calculate performance Draw Down Period.
 *
 * Used by module-performance.epl.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 */
public class DrawDownUtil {

    private static long drawDownPeriod;

    public static long resetDrawDownPeriod() {

        return drawDownPeriod = 0;
    }

    public static long increaseDrawDownPeriod(long milliseconds) {

        drawDownPeriod += milliseconds;

        return drawDownPeriod;
    }
}
