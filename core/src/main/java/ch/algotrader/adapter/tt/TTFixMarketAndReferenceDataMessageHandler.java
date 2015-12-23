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

import ch.algotrader.esper.Engine;
import quickfix.FieldNotFound;
import quickfix.SessionID;
import quickfix.fix42.MarketDataRequestReject;
import quickfix.fix42.MarketDataSnapshotFullRefresh;
import quickfix.fix42.SecurityDefinition;

/**
 * Trading Technologies specific FIX market and reference data handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixMarketAndReferenceDataMessageHandler {

    private final TTFixMarketDataMessageHandler marketDataMessageHandler;
    private final TTFixSecurityDefinitionMessageHandler securityDefinitionMessageHandler;

    public TTFixMarketAndReferenceDataMessageHandler(final Engine serverEngine, final TTPendingRequests pendingRequests) {

        this.marketDataMessageHandler = new TTFixMarketDataMessageHandler(serverEngine);
        this.securityDefinitionMessageHandler = new TTFixSecurityDefinitionMessageHandler(pendingRequests);
    }

    public void onMessage(final MarketDataSnapshotFullRefresh marketData, final SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(marketData, sessionID);
    }

    public void onMessage(final SecurityDefinition securityDefinition, final SessionID sessionID) throws FieldNotFound {

        this.securityDefinitionMessageHandler.onMessage(securityDefinition, sessionID);
    }

    public void onMessage(final MarketDataRequestReject requestReject, final SessionID sessionID) throws FieldNotFound {

        this.marketDataMessageHandler.onMessage(requestReject, sessionID);
    }

}
