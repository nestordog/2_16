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
package ch.algotrader.entity.trade;

import java.io.Serializable;
import java.time.LocalDateTime;

import ch.algotrader.enumeration.Status;

/**
* Execution status of an order.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class ExecutionStatusVO implements Serializable {

    private static final long serialVersionUID = 4573998159302461630L;

    private final String intId;
    private final Status status;
    private final long filledQuantity;
    private final long remainingQuantity;
    private final LocalDateTime dateTime;

    public ExecutionStatusVO(final String intId, final Status status, final long filledQuantity, final long remainingQuantity, final LocalDateTime dateTime) {
        this.intId = intId;
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.dateTime = dateTime;
    }

    public String getIntId() {
        return intId;
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

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() == obj.getClass()) {
            ExecutionStatusVO that = (ExecutionStatusVO) obj;
            return this.intId.equals(that.intId);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return intId.hashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "intId='" + intId + '\'' +
                ", status=" + status +
                ", filledQuantity=" + filledQuantity +
                ", remainingQuantity=" + remainingQuantity +
                ", dateTime=" + dateTime +
                '}';
    }

}
