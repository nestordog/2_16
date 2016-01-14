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
package ch.algotrader.adapter.ftx;

import ch.algotrader.adapter.BrokerAdapterException;
import ch.algotrader.adapter.fix.fix44.Fix44SymbologyResolver;
import ch.algotrader.entity.security.Security;
import quickfix.field.SecurityType;
import quickfix.field.Symbol;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * Fortex symbology resolver implementation.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXSymbologyResolver implements Fix44SymbologyResolver {

    @Override
    public void resolve(final NewOrderSingle message, final Security security, final String broker) throws BrokerAdapterException {

        message.set(new Symbol(FTXUtil.getFTXSymbol(security)));
        message.set(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT));
    }

    @Override
    public void resolve(final OrderCancelReplaceRequest message, final Security security, final String broker) throws BrokerAdapterException {

        message.set(new Symbol(FTXUtil.getFTXSymbol(security)));
        message.set(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT));
    }

    @Override
    public void resolve(final OrderCancelRequest message, final Security security, final String broker) throws BrokerAdapterException {

        message.set(new Symbol(FTXUtil.getFTXSymbol(security)));
        message.set(new SecurityType(SecurityType.FOREIGN_EXCHANGE_CONTRACT));
    }

}
