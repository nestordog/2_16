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
package ch.algotrader.adapter.lmax;

import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.entity.security.Security;

/**
 * LMAX ticker id generator.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class LMAXTickerIdGenerator implements RequestIdGenerator<Security> {

    @Override
    public String generateId(final Security security) {

        String lmaxId = security.getLmaxid();
        if (lmaxId == null) {
            throw new BrokerAdapterException(security + " is not supported by LMAX");
        }
        return lmaxId;
    }

}
