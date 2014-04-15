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
package ch.algotrader.util;

/**
 * Provices null-safe variants of {@code java.lang.Math.abs}
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MathUtil {

    public static Double nullSafeAbs(Double a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }

    public static Integer nullSafeAbs(Integer a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }

    public static Long nullSafeAbs(Long a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }

    public static Float nullSafeAbs(Float a) {
        if (a == null) {
            return null;
        } else {
            return java.lang.Math.abs(a);
        }
    }
}
