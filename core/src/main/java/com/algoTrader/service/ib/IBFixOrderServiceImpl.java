package com.algoTrader.service.ib;

import org.springframework.beans.factory.annotation.Value;

import quickfix.field.Account;
import quickfix.field.AllocationGroup;
import quickfix.field.AllocationMethod;
import quickfix.field.AllocationProfile;
import quickfix.field.ClearingAccount;
import quickfix.field.CustomerOrFirm;
import quickfix.field.ExDestination;
import quickfix.field.HandlInst;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

import com.algoTrader.entity.trade.SimpleOrder;
import com.algoTrader.service.InitializingServiceI;

public class IBFixOrderServiceImpl extends IBFixOrderServiceBase implements InitializingServiceI {

    private static final long serialVersionUID = -537844523983750001L;

    private @Value("${ib.faMethod}") String faMethod;

    @Override
    protected void handleSendOrder(SimpleOrder order, NewOrderSingle newOrder) {

        newOrder.set(new HandlInst('2'));
        newOrder.set(new CustomerOrFirm(0));
        newOrder.set(new ExDestination(order.getSecurity().getSecurityFamily().getMarket().toString()));

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
    }

    @Override
    protected void handleModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        replaceRequest.set(new HandlInst('2'));
        replaceRequest.set(new CustomerOrFirm(0));
        replaceRequest.set(new ExDestination(order.getSecurity().getSecurityFamily().getMarket().toString()));

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
