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
package ch.algotrader.service;

import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.service.ExternalOrderServiceBase;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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
    protected abstract OrderServiceType handleGetOrderServiceType() throws Exception;
}
