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
package ch.algotrader.adapter.ftx;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Forex;
import ch.algotrader.entity.security.Security;
import quickfix.field.QuoteReqID;
import quickfix.field.QuoteRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.QuoteRequest;

/**
 * Fortex market data request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXFixMarketDataRequestFactory {

    public QuoteRequest create(final Security security, final int requestType) {

        if (!(security instanceof Forex)) {

            throw new FixApplicationException("Fortex supports forex orders only");
        }
        Forex forex = (Forex) security;

        String ftxSymbol = FTXUtil.getFTXSymbol(forex);

        QuoteRequest request = new QuoteRequest();
        request.set(new QuoteReqID(ftxSymbol));
        request.setString(Symbol.FIELD, ftxSymbol);
        request.setInt(QuoteRequestType.FIELD, requestType);

        return request;
    }

}
