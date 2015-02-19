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
package ch.algotrader.adapter.ib;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageFactory;
import ch.algotrader.adapter.fix.fix42.GenericFix42SymbologyResolver;
import ch.algotrader.config.IBConfig;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.trade.SimpleOrder;
import quickfix.field.AllocationGroup;
import quickfix.field.AllocationMethod;
import quickfix.field.AllocationProfile;
import quickfix.field.ClearingAccount;
import quickfix.field.CustomerOrFirm;
import quickfix.field.HandlInst;
import quickfix.field.OpenClose;
import quickfix.fix42.NewOrderSingle;
import quickfix.fix42.OrderCancelReplaceRequest;
import quickfix.fix42.OrderCancelRequest;

/**
 *  IB order message factory.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBFixOrderMessageFactory extends GenericFix42OrderMessageFactory {

    private final IBConfig iBConfig;

    public IBFixOrderMessageFactory(final IBConfig iBConfig) {
        super(new GenericFix42SymbologyResolver());

        Validate.notNull(iBConfig, "IBConfig is null");

        this.iBConfig = iBConfig;
    }

    @Override
    public NewOrderSingle createNewOrderMessage(final SimpleOrder order, final String clOrdID) {

        NewOrderSingle message = super.createNewOrderMessage(order, clOrdID);

        message.set(new HandlInst('1'));
        message.set(new CustomerOrFirm(0));

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            message.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            message.set(new AllocationMethod(this.iBConfig.getFaMethod()));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            message.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }

        // add clearing information
        if (order.getAccount().getExtClearingAccount() != null) {
            message.set(new ClearingAccount(order.getAccount().getExtClearingAccount()));
        }

        if (order.getSecurity() instanceof Option) {
            message.set(new OpenClose(OpenClose.OPEN));
        }

        return message;
    }

    @Override
    public OrderCancelReplaceRequest createModifyOrderMessage(final SimpleOrder order, final String clOrdID) {

        OrderCancelReplaceRequest message = super.createModifyOrderMessage(order, clOrdID);

        message.set(new HandlInst('1'));
        message.set(new CustomerOrFirm(0));

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            message.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            message.set(new AllocationMethod(this.iBConfig.getFaMethod()));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            message.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }

        // add clearing information
        if (order.getAccount().getExtClearingAccount() != null) {
            message.set(new ClearingAccount(order.getAccount().getExtClearingAccount()));
        }

        return message;
    }

    @Override
    public OrderCancelRequest createOrderCancelMessage(final SimpleOrder order, final String clOrdID) {

        OrderCancelRequest message = super.createOrderCancelMessage(order, clOrdID);

        // handling for financial advisor account groups
        if (order.getAccount().getExtAccountGroup() != null) {
            message.set(new AllocationGroup(order.getAccount().getExtAccountGroup()));
            message.set(new AllocationMethod(this.iBConfig.getFaMethod()));

            // handling for financial advisor allocation profiles
        } else if (order.getAccount().getExtAllocationProfile() != null) {
            message.set(new AllocationProfile(order.getAccount().getExtAllocationProfile()));
        }

        return message;
    }

}
