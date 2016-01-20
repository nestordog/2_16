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
package ch.algotrader.service.ib;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.ib.IBFixOrderMessageFactory;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.config.IBConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.fix42.Fix42OrderService;
import ch.algotrader.service.fix.fix42.Fix42OrderServiceImpl;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IBFixOrderServiceImpl extends Fix42OrderServiceImpl implements Fix42OrderService {

    public IBFixOrderServiceImpl(
            final String orderServiceType,
            final FixAdapter fixAdapter,
            final OrderBook orderBook,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig,
            final IBConfig iBConfig) {

        super(orderServiceType, fixAdapter, new IBFixOrderMessageFactory(iBConfig),
                orderBook, orderPersistenceService, orderDao, accountDao, commonConfig);
    }

    public IBFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final OrderBook orderBook,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig,
            final IBConfig iBConfig) {

        this(OrderServiceType.IB_FIX.name(), fixAdapter, orderBook, orderPersistenceService,
                orderDao, accountDao, commonConfig, iBConfig);
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
