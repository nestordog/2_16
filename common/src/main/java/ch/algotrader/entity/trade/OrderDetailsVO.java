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

/**
* Details of an open order.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class OrderDetailsVO {

    private final Order order;
    private final ExecutionStatusVO executionStatus;

    public OrderDetailsVO(final Order order, final ExecutionStatusVO executionStatus) {
        this.order = order;
        this.executionStatus = executionStatus;
    }

    public Order getOrder() {
        return order;
    }

    public ExecutionStatusVO getExecutionStatus() {
        return executionStatus;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() == obj.getClass()) {
            OrderDetailsVO that = (OrderDetailsVO) obj;
            return this.executionStatus.getIntId().equals(that.executionStatus.getIntId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.executionStatus.getIntId().hashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "order=" + order +
                ", status=" + executionStatus +
                '}';
    }

}
