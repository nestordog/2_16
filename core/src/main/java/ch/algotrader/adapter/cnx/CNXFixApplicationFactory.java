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
import ch.algotrader.adapter.fix.FixSessionLifecycle;
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

    public CNXFixApplicationFactory(final Object incomingMessageHandler, final Object outgoingMessageHandler, final FixSessionLifecycle lifecycleHandler) {
        super(incomingMessageHandler, outgoingMessageHandler, lifecycleHandler);
    }

    public CNXFixApplicationFactory(final Object incomingMessageHandler, final FixSessionLifecycle lifecycleHandler) {
        super(incomingMessageHandler, null, lifecycleHandler);
    }

    @Override
    protected Application createApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, FixSessionLifecycle lifecycleHandler) {

        return new CNXFixApplication(sessionID, incomingMessageHandler, outgoingMessageHandler, lifecycleHandler);
    }

}
