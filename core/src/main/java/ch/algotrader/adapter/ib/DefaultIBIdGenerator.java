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

/**
 * IB Request and Order Id Generator.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public final class DefaultIBIdGenerator implements IBIdGenerator {

    private int requestId = 1;
    private int orderId = -1;

    @Override
    public synchronized String getNextOrderId() {
        return String.valueOf(this.orderId++);
    }

    @Override
    public synchronized int getNextRequestId() {
        return this.requestId++;
    }

    public void initializeOrderId(int orderId) {
        this.orderId = orderId;
    }

    public boolean isOrderIdInitialized() {
        return this.orderId != -1;
    }
}
