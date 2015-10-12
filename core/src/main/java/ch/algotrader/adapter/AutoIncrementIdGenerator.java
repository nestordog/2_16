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
package ch.algotrader.adapter;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple autoincrement Id Generator.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class AutoIncrementIdGenerator implements IdGenerator {

    private final AtomicLong idCount = new AtomicLong(0);

    @Override
    public long generateId() {
        return this.idCount.incrementAndGet();
    }

    public void update(final long newValue) {

        this.idCount.set(newValue);
    }

    public long updateIfGreater(final long newValue) {

        return this.idCount.updateAndGet(currentValue -> Math.max(currentValue, newValue));
    }

}
