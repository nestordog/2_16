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
package ch.algotrader.adapter.dc;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;

/**
 * DukasCopy utilities
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision: 6424 $ $Date: 2013-11-06 14:29:48 +0100 (Mi, 06 Nov 2013) $
 */
public class DCUtil {

    public static String getDCSymbol(Security security) {

        if (!(security instanceof Forex)) {
            throw new FixApplicationException("DukasCopy can only handle forex");
        }

        Forex forex = (Forex)security;
        return forex.getBaseCurrency() + "/" + forex.getTransactionCurrency();
    }
}
