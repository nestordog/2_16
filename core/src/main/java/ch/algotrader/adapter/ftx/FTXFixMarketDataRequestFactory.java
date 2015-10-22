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

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.RequestIdGenerator;
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

    private final RequestIdGenerator<Security> tickerIdGenerator;

    public FTXFixMarketDataRequestFactory(final RequestIdGenerator<Security> tickerIdGenerator) {

        Validate.notNull(tickerIdGenerator, "RequestIdGenerator is null");

        this.tickerIdGenerator = tickerIdGenerator;
    }

    public QuoteRequest create(final Security security, final int requestType) {

        QuoteRequest request = new QuoteRequest();
        request.set(new QuoteReqID(this.tickerIdGenerator.generateId(security)));
        request.setString(Symbol.FIELD, FTXUtil.getFTXSymbol(security));
        request.setInt(QuoteRequestType.FIELD, requestType);

        return request;
    }

}
