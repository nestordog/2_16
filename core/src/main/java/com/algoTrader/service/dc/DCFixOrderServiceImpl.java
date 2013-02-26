package com.algoTrader.service.dc;

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

import com.algoTrader.entity.security.Forex;
import com.algoTrader.entity.trade.LimitOrder;
import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.entity.trade.StopLimitOrder;
import com.algoTrader.entity.trade.StopOrder;
import com.algoTrader.entity.trade.StopOrderI;

public class DCFixOrderServiceImpl extends DCFixOrderServiceBase {

    private static final long serialVersionUID = -8251827446524602573L;

    @Override
    protected void handleSendOrder(SimpleOrder order, NewOrderSingle newOrder) throws Exception {

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
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) throws Exception {

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
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) throws Exception {

        // set the extId
        cancelRequest.set(new OrderID(order.getExtId()));

        // dc does not support Securitytype
        cancelRequest.removeField(SecurityType.FIELD);
    }
}
