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
package ch.algotrader.service.rt;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix44.GenericFix44SymbologyResolver;
import ch.algotrader.adapter.rt.RTFixOrderMessageFactory;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.fix44.Fix44OrderService;
import ch.algotrader.service.fix.fix44.Fix44OrderServiceImpl;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class RTFixOrderServiceImpl extends Fix44OrderServiceImpl implements Fix44OrderService {

    public RTFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        super(OrderServiceType.RT_FIX.name(), fixAdapter, new RTFixOrderMessageFactory(new GenericFix44SymbologyResolver()),
                orderRegistry, orderPersistenceService, orderDao, accountDao, commonConfig);
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

}
