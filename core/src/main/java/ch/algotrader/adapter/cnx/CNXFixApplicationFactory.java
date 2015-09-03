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
package ch.algotrader.adapter.cnx;

import ch.algotrader.adapter.fix.DefaultFixApplicationFactory;
import ch.algotrader.adapter.ExternalSessionStateHolder;
import quickfix.Application;
import quickfix.SessionID;

/**
 * Creates a {@link ch.algotrader.adapter.cnx.CNXFixApplication} for the specified {@code sessionId}.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXFixApplicationFactory extends DefaultFixApplicationFactory {

    public CNXFixApplicationFactory(final Object incomingMessageHandler, final Object outgoingMessageHandler, final ExternalSessionStateHolder stateHolder) {
        super(incomingMessageHandler, outgoingMessageHandler, stateHolder);
    }

    public CNXFixApplicationFactory(final Object incomingMessageHandler, final ExternalSessionStateHolder stateHolder) {
        super(incomingMessageHandler, null, stateHolder);
    }

    @Override
    protected Application createApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, ExternalSessionStateHolder stateHolder) {

        return new CNXFixApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, stateHolder);
    }

}
