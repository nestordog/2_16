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
package ch.algotrader.adapter.lmax;

import ch.algotrader.adapter.fix.FixApplicationException;
import ch.algotrader.entity.security.Security;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SecurityID;
import quickfix.field.SecurityIDSource;
import quickfix.field.SubscriptionRequestType;
import quickfix.fix44.MarketDataRequest;

/**
 * LMAX market data request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class LMAXFixMarketDataRequestFactory {

    private final LMAXInstrumentCodeMapper mapper;

    public LMAXFixMarketDataRequestFactory(final LMAXInstrumentCodeMapper mapper) {
        this.mapper = mapper;
    }

    public MarketDataRequest create(Security security, SubscriptionRequestType type) {

        String symbol = LMAXUtil.createSymbol(security);
        String code = mapper.mapToCode(symbol);
        if (code == null) {
            throw new FixApplicationException(symbol + " is not supported by LMAX");
        }

        MarketDataRequest request = new MarketDataRequest();
        request.set(new MDReqID(symbol));
        request.set(type);
        request.set(new MarketDepth(1));
        request.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
        symGroup.set(new SecurityID(code));
        symGroup.set(new SecurityIDSource("8"));
        request.addGroup(symGroup);

        return request;
    }

}
