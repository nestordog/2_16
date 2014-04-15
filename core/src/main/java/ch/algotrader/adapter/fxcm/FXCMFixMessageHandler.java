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

import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.fix44.BusinessMessageReject;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MarketDataRequestReject;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import quickfix.fix44.OrderCancelReject;
import quickfix.fix44.QuoteStatusReport;
import quickfix.fix44.Reject;

/**
 * FXCM message handler combining market data feed and order message processing.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FXCMFixMessageHandler {

    private final FXCMFixMarketDataMessageHandler marketDataMessageHandler;
    private final FXCMFixOrderMessageHandler orderMessageHandler;

    public FXCMFixMessageHandler(final FXCMFixMarketDataMessageHandler marketDataMessageHandler, final FXCMFixOrderMessageHandler orderMessageHandler) {
        this.marketDataMessageHandler = marketDataMessageHandler;
        this.orderMessageHandler = orderMessageHandler;
    }

    public void onMessage(final Reject reject, final SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(reject, sessionID);
    }

    public void onMessage(BusinessMessageReject reject, SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(reject, sessionID);
    }

    public void onMessage(MarketDataSnapshotFullRefresh marketData, SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(marketData, sessionID);
    }

    public void onMessage(MarketDataRequestReject requestReject, SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(requestReject, sessionID);
    }

    public void onMessage(QuoteStatusReport quoteStatusReport, SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(quoteStatusReport, sessionID);
    }

    public void onMessage(final ExecutionReport executionReport, final SessionID sessionID) throws FieldNotFound {

        this.orderMessageHandler.onMessage(executionReport, sessionID);
    }

    public void onMessage(final OrderCancelReject reject, final SessionID sessionID) throws FieldNotFound {

        this.orderMessageHandler.onMessage(reject, sessionID);
    }

}
