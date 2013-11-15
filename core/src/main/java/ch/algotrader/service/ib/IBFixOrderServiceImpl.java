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
package ch.algotrader.service.ib;

import org.springframework.beans.factory.annotation.Value;

import quickfix.field.Account;
import quickfix.field.AllocationGroup;
import quickfix.field.AllocationMethod;
import quickfix.field.AllocationProfile;
import quickfix.field.ClearingAccount;
import quickfix.field.CustomerOrFirm;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.field.OpenClose;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.InitializingServiceI;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBFixOrderServiceImpl extends IBFixOrderServiceBase implements InitializingServiceI {

    private static final long serialVersionUID = -537844523983750001L;

    private @Value("${ib.faMethod}") String faMethod;

    @Override
    protected void handleSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        newOrder.set(new HandlInst('2'));
        newOrder.set(new CustomerOrFirm(0));
        newOrder.set(new ExDestination(order.getSecurity().getSecurityFamily().getMarket(order.getAccount().getBroker()).toString()));

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            newOrder.set(new Account(order.getAccount().getExtAccount()));
        }

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            newOrder.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            newOrder.set(new AllocationMethod(this.faMethod));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            newOrder.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }

        // add clearing information
        if (order.getAccount().getExtClearingAccount() != null) {
            newOrder.set(new ClearingAccount(order.getAccount().getExtClearingAccount()));
        }

        if (order.getSecurity() instanceof Option) {
            newOrder.set(new OpenClose(OpenClose.OPEN));
        }
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        replaceRequest.set(new HandlInst('2'));
        replaceRequest.set(new CustomerOrFirm(0));
        replaceRequest.set(new ExDestination(order.getSecurity().getSecurityFamily().getMarket(order.getAccount().getBroker()).toString()));

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            replaceRequest.set(new Account(order.getAccount().getExtAccount()));
        }

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            replaceRequest.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            replaceRequest.set(new AllocationMethod(this.faMethod));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            replaceRequest.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }

        // add clearing information
        if (order.getAccount().getExtClearingAccount() != null) {
            replaceRequest.set(new ClearingAccount(order.getAccount().getExtClearingAccount()));
        }
    }

    @Override
    protected void handleCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            cancelRequest.set(new Account(order.getAccount().getExtAccount()));
        }

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            cancelRequest.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            cancelRequest.set(new AllocationMethod(this.faMethod));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            cancelRequest.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }
    }
}
