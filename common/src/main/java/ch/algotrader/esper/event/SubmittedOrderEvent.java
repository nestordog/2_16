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
package ch.algotrader.esper.event;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Status;

/**
* Esper event to trigger update of the OpenOrder Window..
*/
public class SubmittedOrderEvent {

    private final Order submittedOrder;
    private final Status status;
    private final long filledQuantity;
    private final long remainingQuantity;

    public SubmittedOrderEvent(final Order submittedOrder, final Status status, final long filledQuantity, final long remainingQuantity) {
        Validate.notNull(submittedOrder, "Order is null");
        Validate.notNull(status, "Status is null");
        this.submittedOrder = submittedOrder;
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
    }

    public SubmittedOrderEvent(final Order submittedOrder) {
        Validate.notNull(submittedOrder, "Order is null");
        this.submittedOrder = submittedOrder;
        this.status = Status.OPEN;
        this.filledQuantity = 0;
        this.remainingQuantity = submittedOrder.getQuantity();
    }

    public Order getSubmittedOrder() {
        return submittedOrder;
    }

    public Status getStatus() {
        return status;
    }

    public long getFilledQuantity() {
        return filledQuantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

}
