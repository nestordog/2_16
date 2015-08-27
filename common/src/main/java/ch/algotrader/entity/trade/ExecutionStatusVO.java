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

import ch.algotrader.enumeration.Status;

/**
* Execution status of an order.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class ExecutionStatusVO {

    private final String intId;
    private final Status status;
    private final long filledQuantity;
    private final long remainingQuantity;


    public ExecutionStatusVO(final String intId, final Status status, final long filledQuantity, final long remainingQuantity) {
        this.intId = intId;
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
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

    @Override
    public String toString() {
        return "{" +
                "intId='" + intId + '\'' +
                ", status=" + status +
                ", filledQuantity=" + filledQuantity +
                ", remainingQuantity=" + remainingQuantity +
                '}';
    }

}
