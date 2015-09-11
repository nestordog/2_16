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

import ch.algotrader.enumeration.Status;

public final class IBExecution {

    private Status status;
    private long lastQuantity;
    private long filledQuantity;
    private long remainingQuantity;

    public Status getStatus() {
        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public long getLastQuantity() {
        return lastQuantity;
    }

    public void setLastQuantity(final long lastQuantity) {
        this.lastQuantity = lastQuantity;
    }

    public long getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(final long filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    public long getRemainingQuantity() {
        return remainingQuantity;
    }

    public void setRemainingQuantity(final long remainingQuantity) {
        this.remainingQuantity = remainingQuantity;
    }

    @Override
    public String toString() {
        return "{" +
                "status=" + status +
                ", lastQuantity=" + lastQuantity +
                ", filledQuantity=" + filledQuantity +
                ", remainingQuantity=" + remainingQuantity +
                '}';
    }

}
