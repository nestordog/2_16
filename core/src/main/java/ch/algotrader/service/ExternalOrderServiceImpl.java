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
