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
package ch.algotrader.entity.trade;

import java.io.Serializable;
import java.util.Date;

import ch.algotrader.enumeration.Status;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SubmittedOrder implements Serializable {

    private static final long serialVersionUID = 3350999741885451346L;

    private Status status;

    private long filledQuantity;

    private long remainingQuantity;

    private Order submittedOrder;

    public SubmittedOrder(Status status, long filledQuantity, long remainingQuantity, Order submittedOrder) {
        super();
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.submittedOrder = submittedOrder;
    }

    /**
     * The Order {@link Status}
     * @return this.status Status
     */
    public Status getStatus() {
        return this.status;
    }

    /**
     * The Order {@link Status}
     * @param statusIn Status
     */
    public void setStatus(Status statusIn) {
        this.status = statusIn;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @return this.filledQuantity long
     */
    public long getFilledQuantity() {
        return this.filledQuantity;
    }

    /**
     * The quantity of the Order that has already been filled.
     * @param filledQuantityIn long
     */
    public void setFilledQuantity(long filledQuantityIn) {
        this.filledQuantity = filledQuantityIn;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @return this.remainingQuantity long
     */
    public long getRemainingQuantity() {
        return this.remainingQuantity;
    }

    /**
     * The remaining quantity of the Order that has not been filled yet.
     * @param remainingQuantityIn long
     */
    public void setRemainingQuantity(long remainingQuantityIn) {
        this.remainingQuantity = remainingQuantityIn;
    }

    /**
     * Base Class for all Order Types
     * @return this.submittedOrder Order
     */
    public Order getSubmittedOrder() {
        return this.submittedOrder;
    }

    /**
     * Base Class for all Order Types
     * @param submittedOrderIn Order
     */
    public void setSubmittedOrder(Order submittedOrderIn) {
        this.submittedOrder = submittedOrderIn;
    }

    public Date getDateTime() {
        return submittedOrder.getDateTime();
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getStatus());

        if (getSubmittedOrder() != null) {
            buffer.append(",");
            buffer.append(getSubmittedOrder().getDescription());
        }

        buffer.append(",filledQuantity=");
        buffer.append(getFilledQuantity());
        buffer.append(",remainingQuantity=");
        buffer.append(getRemainingQuantity());

        return buffer.toString();
    }

}
