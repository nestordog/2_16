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

/**
* Details of an open order.
*
* @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
*/
public class OrderDetailsVO implements Serializable {

    private static final long serialVersionUID = 8772503592015087599L;

    private final Order order;
    private final OrderStatusVO orderStatus;

    public OrderDetailsVO(final Order order, final OrderStatusVO orderStatus) {
        this.order = order;
        this.orderStatus = orderStatus;
    }

    public Order getOrder() {
        return order;
    }

    public OrderStatusVO getOrderStatus() {
        return orderStatus;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (getClass() == obj.getClass()) {
            OrderDetailsVO that = (OrderDetailsVO) obj;
            return this.orderStatus.getIntId().equals(that.orderStatus.getIntId());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return this.orderStatus.getIntId().hashCode();
    }

    @Override
    public String toString() {
        return "{" +
                "order=" + order +
                ", status=" + orderStatus +
                '}';
    }

}
