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
package ch.algotrader.adapter.fxcm;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.entity.security.Security;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix44.MarketDataRequest;

/**
 * FXCM market data request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixMarketDataRequestFactory {

    private final RequestIdGenerator<Security> tickerIdGenerator;

    public FXCMFixMarketDataRequestFactory(final RequestIdGenerator<Security> tickerIdGenerator) {

        Validate.notNull(tickerIdGenerator, "RequestIdGenerator is null");

        this.tickerIdGenerator = tickerIdGenerator;
    }

    public MarketDataRequest create(final Security security, final char type) {

        MarketDataRequest request = new MarketDataRequest();
        request.set(new MDReqID(this.tickerIdGenerator.generateId(security)));
        request.set(new SubscriptionRequestType(type));
        request.set(new MarketDepth(0));
        if (SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES == type) {
            request.set(new MDUpdateType(0));
        }

        MarketDataRequest.NoRelatedSym symbol = new MarketDataRequest.NoRelatedSym();
        symbol.set(new Symbol(FXCMUtil.getFXCMSymbol(security)));
        request.addGroup(symbol);

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        return request;
    }

}
