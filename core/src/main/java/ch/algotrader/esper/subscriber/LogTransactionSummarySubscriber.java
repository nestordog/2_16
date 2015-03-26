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
package ch.algotrader.esper.subscriber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.service.TransactionService;

/**
 * Esper event subscriber for {@link ch.algotrader.service.TransactionService#logFillSummary(java.util.List)}.
*/
public class LogTransactionSummarySubscriber {

    public void update(Map<?, ?>[] insertStream, Map<?, ?>[] removeStream) {

        List<Fill> fills = new ArrayList<>();
        for (Map<?, ?> element : insertStream) {
            Fill fill = (Fill) element.get("fill");
            fills.add(fill);
        }

        ServiceLocator.instance().getService("transactionService", TransactionService.class).logFillSummary(fills);
    }
}
