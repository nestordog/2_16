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
package ch.algotrader.service.dc;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix44.GenericFix44OrderMessageFactory;
import ch.algotrader.adapter.fix.fix44.GenericFix44SymbologyResolver;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.ordermgmt.OrderBook;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.fix44.Fix44OrderService;
import ch.algotrader.service.fix.fix44.Fix44OrderServiceImpl;
import ch.algotrader.util.PriceUtil;
import quickfix.field.OrdType;
import quickfix.field.OrderID;
import quickfix.field.Price;
import quickfix.field.SecurityType;
import quickfix.field.StopPx;
import quickfix.field.Symbol;
import quickfix.field.TimeInForce;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * DukasCopy order service implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class DCFixOrderServiceImpl extends Fix44OrderServiceImpl implements Fix44OrderService {

    public DCFixOrderServiceImpl(
            final String orderServiceType,
            final FixAdapter fixAdapter,
            final OrderBook orderBook,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        super(orderServiceType, fixAdapter,
                new GenericFix44OrderMessageFactory(new GenericFix44SymbologyResolver()),
                orderBook, orderPersistenceService, orderDao, accountDao, commonConfig);
    }

    public DCFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final OrderBook orderBook,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        this(OrderServiceType.DC_FIX.name(), fixAdapter, orderBook, orderPersistenceService,
                orderDao, accountDao, commonConfig);
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(newOrder, "New order is null");

        if (!(order.getSecurity() instanceof Forex)) {
            throw new IllegalArgumentException("DukasCopy can only handle Forex");
        }

        if (order instanceof StopLimitOrder) {
            throw new IllegalArgumentException("DukasCopy does not support StopLimitOrders");
        }

        // Note: DukasCopy uses StopLimit for Limit orders
        if (order instanceof LimitOrder) {
            newOrder.set(new OrdType(OrdType.STOP_LIMIT));
        }

        // Note: DukasCopy uses Price for Stop orders instead of StopPx
        if (order instanceof StopOrder) {
            StopOrder stopOrder = (StopOrder) order;
            newOrder.removeField(StopPx.FIELD);
            newOrder.set(new Price(PriceUtil.denormalizePrice(order, stopOrder.getStop())));
        }

        newOrder.set(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        newOrder.set(new Symbol(order.getSecurity().getSymbol().replace(".", "/")));

        // dc does not support Securitytype
        newOrder.removeField(SecurityType.FIELD);

    }

    @Override
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(replaceRequest, "Replace request is null");
        Validate.notNull(order.getExtId(), "missing ExtId on order");

        // Note: DukasCopy uses StopLimit for Limit orders
        if (order instanceof LimitOrder) {
            replaceRequest.set(new OrdType(OrdType.STOP_LIMIT));
        }

        // Note: DukasCopy uses Price for Stop orders instead of StopPx
        if (order instanceof StopOrder) {
            replaceRequest.removeField(StopPx.FIELD);
            StopOrderI stopOrder = (StopOrderI) order;
            replaceRequest.set(new Price(PriceUtil.denormalizePrice(order, stopOrder.getStop())));
        }

        replaceRequest.set(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
        replaceRequest.set(new Symbol(order.getSecurity().getSymbol().replace(".", "/")));

        // set the extId
        replaceRequest.set(new OrderID(order.getExtId()));

        // dc does not support Securitytype
        replaceRequest.removeField(SecurityType.FIELD);

    }

    @Override
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(cancelRequest, "Cancel request is null");
        Validate.notNull(order.getExtId(), "missing ExtId on order");

        // set the extId
        cancelRequest.set(new OrderID(order.getExtId()));

        // dc does not support Securitytype
        cancelRequest.removeField(SecurityType.FIELD);

    }

}
