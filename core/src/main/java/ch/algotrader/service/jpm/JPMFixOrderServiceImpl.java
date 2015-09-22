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
package ch.algotrader.service.jpm;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42SymbologyResolver;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.fix42.Fix42OrderService;
import ch.algotrader.service.fix.fix42.Fix42OrderServiceImpl;
import quickfix.field.HandlInst;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class JPMFixOrderServiceImpl extends Fix42OrderServiceImpl implements Fix42OrderService {

    private static final long serialVersionUID = -8881034489922372443L;

    public JPMFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final CommonConfig commonConfig) {

        super(fixAdapter, orderRegistry, orderPersistenceService, new GenericFix42OrderMessageFactory(new GenericFix42SymbologyResolver()), commonConfig);
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {
        newOrder.set(new HandlInst('1'));
    }

    @Override
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {
        replaceRequest.set(new HandlInst('1'));
    }

    @Override
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {
    }

    @Override
    public String getOrderServiceType() {

        return OrderServiceType.JPM_FIX.name();
    }
}
