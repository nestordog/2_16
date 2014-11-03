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
package ch.algotrader.service.ib;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42SymbologyResolver;
import ch.algotrader.config.IBConfig;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.fix42.Fix42OrderServiceImpl;
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

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBFixOrderServiceImpl extends Fix42OrderServiceImpl implements IBFixOrderService, InitializingServiceI {

    private static final long serialVersionUID = -537844523983750001L;

    private final IBConfig iBConfig;

    public IBFixOrderServiceImpl(final IBConfig iBConfig,
            final FixAdapter fixAdapter,
            final OrderService orderService) {

        super(fixAdapter, orderService, new GenericFix42OrderMessageFactory(new GenericFix42SymbologyResolver()));

        Validate.notNull(iBConfig, "IBConfig is null");

        this.iBConfig = iBConfig;
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(newOrder, "New order is null");

        newOrder.set(new HandlInst('2'));
        newOrder.set(new CustomerOrFirm(0));
        newOrder.set(new ExDestination(order.getSecurity().getSecurityFamily().getExchangeCode(order.getAccount().getBroker()).toString()));

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            newOrder.set(new Account(order.getAccount().getExtAccount()));
        }

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            newOrder.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            newOrder.set(new AllocationMethod(this.iBConfig.getFaMethod()));

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
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(replaceRequest, "Replace request is null");

        replaceRequest.set(new HandlInst('2'));
        replaceRequest.set(new CustomerOrFirm(0));
        replaceRequest.set(new ExDestination(order.getSecurity().getSecurityFamily().getExchangeCode(order.getAccount().getBroker()).toString()));

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            replaceRequest.set(new Account(order.getAccount().getExtAccount()));
        }

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            replaceRequest.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            replaceRequest.set(new AllocationMethod(this.iBConfig.getFaMethod()));

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
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {

        Validate.notNull(order, "Order is null");
        Validate.notNull(cancelRequest, "Cancel request is null");

        // handling for accounts
        if (order.getAccount().getExtAccount() != null) {
            cancelRequest.set(new Account(order.getAccount().getExtAccount()));
        }

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            cancelRequest.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            cancelRequest.set(new AllocationMethod(this.iBConfig.getFaMethod()));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            cancelRequest.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }

    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.IB_FIX;
    }
}
