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
package ch.algotrader.service.dc;

import org.apache.commons.lang.Validate;

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
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.trade.LimitOrder;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.entity.trade.StopLimitOrder;
import ch.algotrader.entity.trade.StopOrder;
import ch.algotrader.entity.trade.StopOrderI;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.fix44.Fix44OrderServiceImpl;

/**
 * DukasCopy order service implementation.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class DCFixOrderServiceImpl extends Fix44OrderServiceImpl implements DCFixOrderService {

    private static final long serialVersionUID = -8251827446524602573L;

    public DCFixOrderServiceImpl(final FixAdapter fixAdapter,
            final OrderService orderService) {

        super(fixAdapter, orderService);
    }

    @Override
    public void sendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(newOrder, "New order is null");

        try {
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
                newOrder.removeField(StopPx.FIELD);
                newOrder.set(new Price(((StopOrderI) order).getStop().doubleValue()));
            }

            newOrder.set(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
            newOrder.set(new Symbol(order.getSecurity().getSymbol().replace(".", "/")));

            // dc does not support Securitytype
            newOrder.removeField(SecurityType.FIELD);
        } catch (Exception ex) {
            throw new DCFixOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void modifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(replaceRequest, "Replace request is null");
        Validate.notNull(order.getExtId(), "missing ExtId on order");

        try {
            // Note: DukasCopy uses StopLimit for Limit orders
            if (order instanceof LimitOrder) {
                replaceRequest.set(new OrdType(OrdType.STOP_LIMIT));
            }

            // Note: DukasCopy uses Price for Stop orders instead of StopPx
            if (order instanceof StopOrder) {
                replaceRequest.removeField(StopPx.FIELD);
                replaceRequest.set(new Price(((StopOrderI) order).getStop().doubleValue()));
            }

            replaceRequest.set(new TimeInForce(TimeInForce.GOOD_TILL_CANCEL));
            replaceRequest.set(new Symbol(order.getSecurity().getSymbol().replace(".", "/")));

            // set the extId
            replaceRequest.set(new OrderID(order.getExtId()));

            // dc does not support Securitytype
            replaceRequest.removeField(SecurityType.FIELD);
        } catch (Exception ex) {
            throw new DCFixOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void cancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(cancelRequest, "Cancel request is null");
        Validate.notNull(order.getExtId(), "missing ExtId on order");

        try {
            // set the extId
            cancelRequest.set(new OrderID(order.getExtId()));

            // dc does not support Securitytype
            cancelRequest.removeField(SecurityType.FIELD);
        } catch (Exception ex) {
            throw new DCFixOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.DC_FIX;
    }
}
