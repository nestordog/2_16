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
package ch.algotrader.adapter.ib;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * IB Request and Order Id Generator.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class DefaultIBIdGenerator implements IBIdGenerator {

    private final AtomicInteger requestId = new AtomicInteger(-1);
    private final AtomicLong orderId =  new AtomicLong(-1);

    @Override
    public String getNextOrderId() {
        return Long.toString(this.orderId.incrementAndGet());
    }

    @Override
    public int getNextRequestId() {
        return this.requestId.incrementAndGet();
    }

    @Override
    public void initializeOrderId(long orderId) {
        this.orderId.set(orderId);
    }

    public boolean isOrderIdInitialized() {
        return this.orderId.get() != -1;
    }
}
