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
package ch.algotrader.entity.security;

import ch.algotrader.entity.marketData.Tick;
import ch.algotrader.enumeration.Currency;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ForexImpl extends Forex {

    private static final long serialVersionUID = -6204294412084812111L;

    @Override
    public Currency getTransactionCurrency() {

        return getSecurityFamily().getCurrency();
    }

    @Override
    public boolean validateTick(Tick tick) {

        if (tick.getBid() == null || tick.getBid().doubleValue() < 0) {
            return false;
        } else if (tick.getAsk() == null || tick.getAsk().doubleValue() < 0) {
            return false;
        } else {
            return true;
        }

    }
}
