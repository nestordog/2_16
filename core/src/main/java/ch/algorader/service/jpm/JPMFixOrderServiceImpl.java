/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.service.jpm;

import java.util.Date;

import quickfix.field.Account;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.field.SecurityExchange;
import quickfix.field.TransactTime;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.enumeration.Market;
import com.algoTrader.service.jpm.JPMFixOrderServiceBase;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class JPMFixOrderServiceImpl extends JPMFixOrderServiceBase {

    private static final long serialVersionUID = -8881034489922372443L;

    @Override
    protected void handleSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        newOrder.set(new Account(order.getAccount().getExtAccount()));
        newOrder.set(new HandlInst('1'));
        newOrder.set(new TransactTime(new Date()));

        String exchange = getExchange(order.getSecurity().getSecurityFamily().getMarket());
        newOrder.set(new ExDestination(exchange));
        newOrder.set(new SecurityExchange(exchange));
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        replaceRequest.set(new Account(order.getAccount().getExtAccount()));
        replaceRequest.set(new HandlInst('1'));
        replaceRequest.set(new TransactTime(new Date()));

        String exchange = getExchange(order.getSecurity().getSecurityFamily().getMarket());
        replaceRequest.set(new ExDestination(exchange));
        replaceRequest.set(new SecurityExchange(exchange));
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        cancelRequest.set(new Account(order.getAccount().getExtAccount()));
        cancelRequest.set(new TransactTime(new Date()));

        String exchange = getExchange(order.getSecurity().getSecurityFamily().getMarket());
        cancelRequest.set(new SecurityExchange(exchange));
    }

    private String getExchange(Market market) {

        if (Market.CBOE.equals(market)) {
            return "XCBO";
        } else if (Market.CFE.equals(market)) {
            return "XCBF";
        } else {
            throw new UnsupportedOperationException("market not supported " + market);
        }
    }
}
