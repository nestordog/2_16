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
package ch.algotrader.service.tt;

import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.tt.TTFixOrderMessageFactory;
import ch.algotrader.adapter.tt.TTFixOrderStatusRequestFactory;
import ch.algotrader.adapter.tt.TTFixPositionRequestFactory;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.FixStatelessService;
import ch.algotrader.service.fix.fix42.Fix42OrderService;
import ch.algotrader.service.fix.fix42.Fix42OrderServiceImpl;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixOrderServiceImpl extends Fix42OrderServiceImpl implements Fix42OrderService, FixStatelessService {

    private static final Logger LOGGER = LogManager.getLogger(TTFixOrderServiceImpl.class);

    private final TTFixPositionRequestFactory positionRequestFactory;
    private final TTFixOrderStatusRequestFactory orderStatusRequestFactory;

    public TTFixOrderServiceImpl(
            final String orderServiceType,
            final FixAdapter fixAdapter,
            final ExternalSessionStateHolder stateHolder,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        super(orderServiceType, fixAdapter, stateHolder, new TTFixOrderMessageFactory(),
                orderRegistry, orderPersistenceService, orderDao, accountDao, commonConfig);
        this.positionRequestFactory = new TTFixPositionRequestFactory();
        this.orderStatusRequestFactory = new TTFixOrderStatusRequestFactory();
    }

    public TTFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final ExternalSessionStateHolder stateHolder,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        this(OrderServiceType.TT_FIX.name(), fixAdapter, stateHolder, orderRegistry, orderPersistenceService,
                orderDao, accountDao, commonConfig);
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
    public void requestStateUpdate() {

        Set<String> sessionQualifiers = getAllSessionQualifiers();
        for (String sessionQualifier: sessionQualifiers) {

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Requesting session state for {}", sessionQualifier);
            }

            getFixAdapter().sendMessage(this.positionRequestFactory.create(), sessionQualifier);
            List<Order> allOpenOrders = getOrderRegistry().getAllOpenOrders();
            for (Order order: allOpenOrders) {

                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("Requesting order state: {}", order);
                }
                getFixAdapter().sendMessage(this.orderStatusRequestFactory.create(order), sessionQualifier);
            }
        }
    }

}
