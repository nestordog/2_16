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
package ch.algotrader.entity.security;

import ch.algotrader.enumeration.Currency;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ForexImpl extends Forex {

    private static final long serialVersionUID = -6204294412084812111L;

    @Override
    public Currency getTransactionCurrency() {

        return getSecurityFamily().getCurrency();
    }
}
