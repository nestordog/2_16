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

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.AbstractFixApplication;
import ch.algotrader.adapter.ExternalSessionStateHolder;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;
import quickfix.field.Text;
import quickfix.field.TradSesStatus;
import quickfix.fix44.TradingSessionStatus;

/**
 * Currenex specific {@link quickfix.Application} that implements logon handshake
 * involving {@link quickfix.fix44.TradingSessionStatus} acknowledgement message.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class CNXFixApplication extends AbstractFixApplication {

    private static final Logger LOGGER = LogManager.getLogger(CNXFixApplication.class);

    private final ExternalSessionStateHolder stateHolder;

    public CNXFixApplication(SessionID sessionID, Object incomingMessageHandler, Object outgoingMessageHandler, ExternalSessionStateHolder stateHolder) {
        super(sessionID, incomingMessageHandler, outgoingMessageHandler);

        Validate.notNull(sessionID, "Session ID may not be null");
        this.stateHolder = stateHolder;
    }

    @Override
    public void onCreate() {

        stateHolder.onCreate();
    }

    @Override
    protected boolean interceptIncoming(Message message) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        if (message instanceof TradingSessionStatus) {

            TradingSessionStatus tradingSessionStatus = (TradingSessionStatus) message;
            TradSesStatus status = tradingSessionStatus.getTradSesStatus();
            if (status.getValue() == TradSesStatus.OPEN) {

                stateHolder.onLogon();
            } else {

                if (tradingSessionStatus.isSetText()) {

                    Text text = tradingSessionStatus.getText();
                    LOGGER.error("CNX session failed: {}", text.getValue());
                } else {

                    LOGGER.error("CNX session failed with status code {}", status.getValue());
                }
            }

            return true;
        }
        return super.interceptIncoming(message);
    }

    @Override
    public void onLogout() {

        stateHolder.onLogoff();
    }

}
