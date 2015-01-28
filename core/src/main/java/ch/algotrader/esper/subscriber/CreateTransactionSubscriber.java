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

import org.apache.log4j.Logger;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.trade.Fill;
import ch.algotrader.service.TransactionService;
import ch.algotrader.util.metric.MetricsUtil;

/**
* Esper event subscriber for {@link ch.algotrader.service.TransactionService#createTransaction(ch.algotrader.entity.trade.Fill)}.
*/
public class CreateTransactionSubscriber extends Subscriber {

    private static Logger LOGGER = Logger.getLogger(CreateTransactionSubscriber.class.getName());

    /*
     * synchronized to make sure that only on transaction is created at a time
     */
    public void update(Fill fill) {

        long startTime = System.nanoTime();
        LOGGER.debug("createTransaction start");
        ServiceLocator.instance().getService("transactionService", TransactionService.class).createTransaction(fill);
        LOGGER.debug("createTransaction end");
        MetricsUtil.accountEnd("CreateTransactionSubscriber", startTime);
    }
}
