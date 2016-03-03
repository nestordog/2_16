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
package ch.algotrader.entity.trade.algo;

import ch.algotrader.enumeration.Status;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TargetPositionOrderStateVO extends AlgoOrderStateVO {

    private static final long serialVersionUID = 1L;

    private volatile long targetQty;
    private volatile long actualQty;
    private volatile Status orderStatus;

    public long getTargetQty() {
        return targetQty;
    }

    public void setTargetQty(final long targetQty) {
        this.targetQty = targetQty;
    }

    public long getActualQty() {
        return actualQty;
    }

    public void setActualQty(final long actualQty) {
        this.actualQty = actualQty;
    }

    public Status getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(Status orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("target=").append(targetQty).append("; actual=").append(actualQty);
        String intId = getIntId();
        if (intId != null) {
            buf.append("working order=").append(intId).append(" (").append(orderStatus).append(")");
        }
        return buf.toString();
    }

}
