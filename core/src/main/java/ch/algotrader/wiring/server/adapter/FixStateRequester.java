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
package ch.algotrader.wiring.server.adapter;

import org.apache.commons.lang.Validate;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.enumeration.ConnectionState;
import ch.algotrader.event.listener.SessionEventListener;
import ch.algotrader.service.fix.FixStatelessService;
import ch.algotrader.vo.SessionEventVO;

public class FixStateRequester implements SessionEventListener {

    private final FixStatelessService statelessService;
    private final ExternalSessionStateHolder sessionStateHolder;

    public FixStateRequester(final FixStatelessService statelessService, final ExternalSessionStateHolder sessionStateHolder) {

        Validate.notNull(statelessService, "FixStatelessService is null");

        this.statelessService = statelessService;
        this.sessionStateHolder = sessionStateHolder;
    }

    @Override
    public void onChange(final SessionEventVO event) {
        if (event.getState() == ConnectionState.LOGGED_ON) {
            if (this.sessionStateHolder.getName().equals(event.getQualifier())) {

                this.statelessService.requestStateUpdate();
            }
        }
    }

}
