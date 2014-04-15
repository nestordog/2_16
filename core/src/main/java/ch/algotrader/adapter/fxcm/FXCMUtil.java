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
package ch.algotrader.adapter.fxcm;

import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;

/**
 * LMAX utilities
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMUtil {

    public static String getFXCMSymbol(final Security security) {
        if (security instanceof Forex) {
            Forex forex = (Forex) security;
            return forex.getBaseCurrency() + "/" + forex.getTransactionCurrency();
        } else {
            return security.getSymbol();
        }
    }

}
