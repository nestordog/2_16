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
package ch.algotrader.service.ib;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.ib.IBFixOrderMessageFactory;
import ch.algotrader.config.IBConfig;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OpenOrderRegistry;
import ch.algotrader.service.fix.fix42.Fix42OrderService;
import ch.algotrader.service.fix.fix42.Fix42OrderServiceImpl;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBFixOrderServiceImpl extends Fix42OrderServiceImpl implements Fix42OrderService {

    private static final long serialVersionUID = -537844523983750001L;

    public IBFixOrderServiceImpl(final IBConfig iBConfig, final FixAdapter fixAdapter, final OpenOrderRegistry openOrderRegistry) {

        super(fixAdapter, openOrderRegistry, new IBFixOrderMessageFactory(iBConfig));
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {
    }

    @Override
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {
    }

    @Override
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {
    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.IB_FIX;
    }
}
