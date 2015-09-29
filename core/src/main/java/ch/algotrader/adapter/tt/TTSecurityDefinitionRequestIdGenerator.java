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
package ch.algotrader.adapter.tt;

import java.util.concurrent.atomic.AtomicLong;

import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.entity.security.SecurityFamily;

/**
 * Trading technologies security definition request id generator.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTSecurityDefinitionRequestIdGenerator implements RequestIdGenerator<SecurityFamily> {

    private final AtomicLong count = new AtomicLong(0);

    @Override
    public String generateId(final SecurityFamily securityFamily) {

        return "at-" + this.count.incrementAndGet();
    }

}
