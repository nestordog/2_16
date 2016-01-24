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
package ch.algotrader.adapter.tt;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.RequestIdGenerator;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Broker;
import quickfix.field.AggregatedBook;
import quickfix.field.MDEntryType;
import quickfix.field.MDReqID;
import quickfix.field.MDUpdateType;
import quickfix.field.MarketDepth;
import quickfix.field.SecurityExchange;
import quickfix.field.SecurityID;
import quickfix.field.SubscriptionRequestType;
import quickfix.field.Symbol;
import quickfix.fix42.MarketDataRequest;

/**
 * Trading Technologies market data request factory.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixMarketDataRequestFactory {

    private final RequestIdGenerator<Security> tickerIdGenerator;

    public TTFixMarketDataRequestFactory(final RequestIdGenerator<Security> tickerIdGenerator) {

        Validate.notNull(tickerIdGenerator, "RequestIdGenerator is null");

        this.tickerIdGenerator = tickerIdGenerator;
    }

    public MarketDataRequest create(final Security security, final char requestType) {

        Validate.notNull(security, "Security is null");
        Validate.notNull(security.getTtid(), "Security TT id is null");

        MarketDataRequest request = new MarketDataRequest();
        request.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
        request.set(new MDReqID(this.tickerIdGenerator.generateId(security)));
        request.set(new SubscriptionRequestType(requestType));

        request.set(new MarketDepth(1));
        request.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
        request.set(new AggregatedBook(true));

        MarketDataRequest.NoMDEntryTypes bid = new MarketDataRequest.NoMDEntryTypes();
        bid.set(new MDEntryType(MDEntryType.BID));
        request.addGroup(bid);

        MarketDataRequest.NoMDEntryTypes offer = new MarketDataRequest.NoMDEntryTypes();
        offer.set(new MDEntryType(MDEntryType.OFFER));
        request.addGroup(offer);

        MarketDataRequest.NoMDEntryTypes trade = new MarketDataRequest.NoMDEntryTypes();
        trade.set(new MDEntryType(MDEntryType.TRADE));
        request.addGroup(trade);

        MarketDataRequest.NoRelatedSym symbol = new MarketDataRequest.NoRelatedSym();

        SecurityFamily securityFamily = security.getSecurityFamily();
        Exchange exchange = securityFamily.getExchange();
        symbol.set(new SecurityExchange(exchange.getCode()));
        symbol.set(new Symbol(securityFamily.getSymbolRoot(Broker.TT.name())));
        symbol.set(new SecurityID(security.getTtid()));
        request.addGroup(symbol);

        return request;
    }

}
