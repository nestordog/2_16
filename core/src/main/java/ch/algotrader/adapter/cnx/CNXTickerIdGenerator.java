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
package ch.algotrader.adapter.cnx;

import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;

/**
 * Currenex ticker id generator.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class CNXTickerIdGenerator implements RequestIdGenerator<Security> {

    @Override
    public String generateId(final Security security) {

        if (!(security instanceof Forex)) {
            throw new FixApplicationException("Currenex supports Forex only");
        }
        return CNXUtil.getCNXSymbol((Forex) security);
    }

}
