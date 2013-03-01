/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.service;

import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.OrderServiceType;

public abstract class ExternalOrderServiceImpl extends ExternalOrderServiceBase {

    @Override
    protected abstract void handleSendOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract void handleValidateOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract void handleCancelOrder(SimpleOrder order) throws Exception;

    @Override
    protected abstract void handleModifyOrder(SimpleOrder order) throws Exception;

    @Override
    protected OrderServiceType handleGetOrderServiceType() throws Exception {

        return OrderServiceType.fromValue(this.getClass().getName().split("Impl")[0]);
    }
}
