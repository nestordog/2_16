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

import ch.algotrader.entity.trade.LimitOrder;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class TrailingLimitOrderStateVO extends AlgoOrderStateVO {

    private static final long serialVersionUID = 1L;

    private LimitOrder limitOrder;

    public LimitOrder getLimitOrder() {
        return this.limitOrder;
    }

    public void setLimitOrder(LimitOrder limitOrder) {
        this.limitOrder = limitOrder;
    }

    @Override
    public String toString() {
        return "limitOrder=" + this.limitOrder;
    }


}
