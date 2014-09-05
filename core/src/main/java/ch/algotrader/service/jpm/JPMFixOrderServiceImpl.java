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

import java.util.Date;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42SymbologyResolver;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.fix42.Fix42OrderServiceImpl;
import quickfix.field.Account;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.field.SecurityExchange;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class JPMFixOrderServiceImpl extends Fix42OrderServiceImpl implements JPMFixOrderService {

    private static final long serialVersionUID = -8881034489922372443L;

    public JPMFixOrderServiceImpl(final FixAdapter fixAdapter,
            final OrderService orderService) {

        super(fixAdapter, orderService, new GenericFix42OrderMessageFactory(new GenericFix42SymbologyResolver()));
    }

    @Override
    public void sendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(newOrder, "New order is null");

        try {
            newOrder.set(new Account(order.getAccount().getExtAccount()));
            newOrder.set(new HandlInst('1'));
            newOrder.set(new TransactTime(new Date()));

            String exchange = order.getSecurity().getSecurityFamily().getExchangeCode(Broker.JPM);
            newOrder.set(new ExDestination(exchange));
            newOrder.set(new SecurityExchange(exchange));
        } catch (Exception ex) {
            throw new JPMFixOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void modifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(replaceRequest, "Replace request is null");

        try {
            replaceRequest.set(new Account(order.getAccount().getExtAccount()));
            replaceRequest.set(new HandlInst('1'));
            replaceRequest.set(new TransactTime(new Date()));

            String exchange = order.getSecurity().getSecurityFamily().getExchangeCode(Broker.JPM);
            replaceRequest.set(new ExDestination(exchange));
            replaceRequest.set(new SecurityExchange(exchange));
        } catch (Exception ex) {
            throw new JPMFixOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public void cancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(cancelRequest, "Cancel request is null");

        try {
            cancelRequest.set(new Account(order.getAccount().getExtAccount()));
            cancelRequest.set(new TransactTime(new Date()));

            String exchange = order.getSecurity().getSecurityFamily().getExchangeCode(Broker.JPM);
            cancelRequest.set(new SecurityExchange(exchange));
        } catch (Exception ex) {
            throw new JPMFixOrderServiceException(ex.getMessage(), ex);
        }
    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.JPM_FIX;
    }
}
