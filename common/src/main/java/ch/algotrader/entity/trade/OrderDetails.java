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

/**
* Details of an open order.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class OrderDetails {

    private final Order order;
    private final ExecutionStatus executionStatus;

    public OrderDetails(final Order order, final ExecutionStatus executionStatus) {
        this.order = order;
        this.executionStatus = executionStatus;
    }

    public Order getOrder() {
        return order;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    @Override
    public String toString() {
        return "{" +
                "order=" + order +
                ", status=" + executionStatus +
                '}';
    }

}
