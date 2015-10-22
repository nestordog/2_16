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
package ch.algotrader.esper.callback;

import java.util.List;

import ch.algotrader.entity.trade.OrderStatusVO;

/**
 * Esper Callback Class that will throw an exception unluss all {@code orders} passed to {@link ch.algotrader.esper.Engine#addTradeCallback} have been fully exectured.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class FullExecutionTradeCallback extends TradeCallback {

    public FullExecutionTradeCallback() {
        super(true);
    }

    @Override
    public void onTradeCompleted(List<OrderStatusVO> orderStatus) throws Exception {
        // do nothing
    }
}
