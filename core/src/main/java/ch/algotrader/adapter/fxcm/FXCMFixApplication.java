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
package ch.algotrader.adapter.fxcm;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.AbstractFixApplication;
import ch.algotrader.adapter.ExternalSessionStateHolder;
import quickfix.ConfigError;
import quickfix.FieldConvertError;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.UnsupportedMessageType;
import quickfix.field.Password;
import quickfix.field.UserRequestID;
import quickfix.field.UserRequestType;
import quickfix.field.UserStatus;
import quickfix.field.UserStatusText;
import quickfix.field.Username;
import quickfix.fix44.UserRequest;
import quickfix.fix44.UserResponse;

/**
 * FXCM specific {@link quickfix.Application} that implements FXCM user authentication protocol
 * based on {@link quickfix.fix44.UserRequest} / {@link quickfix.fix44.UserResponse} exchange.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FXCMFixApplication extends AbstractFixApplication {

    private static final Logger LOGGER = LogManager.getLogger(FXCMFixApplication.class);

    private final SessionSettings settings;
    private final ExternalSessionStateHolder stateHolder;

    public FXCMFixApplication(SessionID sessionID, Object incomingMessageHandler, SessionSettings settings, ExternalSessionStateHolder stateHolder) {
        super(sessionID, incomingMessageHandler, null);

        Validate.notNull(settings, "Session settings not be null");
        Validate.notNull(sessionID, "Session ID may not be null");
        this.settings = settings;
        this.stateHolder = stateHolder;
    }

    @Override
    public void onCreate() {

        stateHolder.onCreate();
    }

    @Override
    public void onLogon() {

        SessionID sessionID = getSessionID();
        try {
            UserRequest userRequest = new UserRequest();
            userRequest.set(new Username(settings.getString(sessionID, "Username")));
            userRequest.set(new Password(settings.getString(sessionID, "Password")));
            userRequest.set(new UserRequestID("1"));
            userRequest.set(new UserRequestType(UserRequestType.LOGONUSER));
            getSession().send(userRequest);
        } catch (ConfigError ex) {
            LOGGER.error("Session confguration error: {}", ex.getMessage());
        } catch (FieldConvertError ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
    }

    @Override
    protected boolean interceptIncoming(Message message) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {

        if (message instanceof UserResponse) {

            UserResponse userResponse = (UserResponse) message;
            UserStatus userStatus = userResponse.getUserStatus();
            if (userStatus.getValue() == UserStatus.LOGGED_IN) {
                stateHolder.onLogon();
            } else {
                UserStatusText userStatusText = userResponse.getUserStatusText();
                LOGGER.error("FXCM logon failed: {}", userStatusText.getValue());
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
