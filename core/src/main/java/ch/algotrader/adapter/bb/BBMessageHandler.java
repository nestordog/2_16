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
package ch.algotrader.adapter.bb;

import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;

import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.EventHandler;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Name;
import com.bloomberglp.blpapi.Session;

/**
 * Bloomberg MessageHandler.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision: 5941 $ $Date: 2013-05-31 13:23:59 +0200 (Fr, 31 Mai 2013) $
 */
public abstract class BBMessageHandler implements EventHandler {

    private static Logger logger = MyLogger.getLogger(BBMessageHandler.class.getName());

    private static final Name SESSION_CONNECTION_UP = Name.getName("SessionConnectionUp");
    private static final Name SESSION_STARTED = Name.getName("SessionStarted");
    private static final Name SESSION_TERMINATED = Name.getName("SessionTerminated");
    private static final Name SESSION_STARTUP_FAILURE = Name.getName("SessionStartupFailure");

    private static final Name SERVICE_OPENED = Name.getName("ServiceOpened");

    private final Object lock = new Object();
    private boolean running;

    @Override
    public void processEvent(Event event, Session session) {

        try {
            internalProcessEvent(session, event);
        } catch (Exception e) {
            logger.error("problem processing event", e);
            for (Message msg : event) {
                logger.error("correlationID: " + msg.correlationID().value() + ", " + msg);
            }
        }
    }

    public boolean processEvent(Session session) throws InterruptedException {

        Event event = session.nextEvent();

        return internalProcessEvent(session, event);
    }

    private boolean internalProcessEvent(Session session, Event event) {

        switch (event.eventType().intValue()) {
            case Event.EventType.Constants.SESSION_STATUS:
                synchronized (this.lock) {
                    processSessionStatus(event, session);
                }
                return false;
            case Event.EventType.Constants.PARTIAL_RESPONSE:
                processResponseEvent(event, session);
                return false;
            case Event.EventType.Constants.RESPONSE:
                processResponseEvent(event, session);
                return true;
            case Event.EventType.Constants.SUBSCRIPTION_STATUS:
                synchronized (this.lock) {
                    processSubscriptionStatus(event, session);
                }
                return false;
            case Event.EventType.Constants.SUBSCRIPTION_DATA:
                processSubscriptionDataEvent(event, session);
                return false;
            case Event.EventType.Constants.ADMIN:
                synchronized (this.lock) {
                    processAdminEvent(event, session);
                }
                return false;
            default:
                processMiscEvents(event, session);
                return false;
        }
    }

    private void processSessionStatus(Event event, Session session) {

        for (Message msg : event) {

            if (msg.messageType() == SESSION_CONNECTION_UP) {
                logger.info("session connection up");
            } else if (msg.messageType() == SESSION_STARTED) {
                this.running = true;
                logger.info("session started");
            } else if (msg.messageType() == SESSION_TERMINATED) {
                this.running = false;
                logger.info("session terminated");
            } else if (msg.messageType() == SESSION_STARTUP_FAILURE) {
                logger.error(msg);
            } else {
                throw new IllegalStateException("unknown messageType " + msg.messageType());
            }
        }
    }

    private void processAdminEvent(Event event, Session session) {

        for (Message msg : event) {
            logger.info(msg);
        }
    }

    private void processMiscEvents(Event event, Session session) {

        for (Message msg : event) {
            if (msg.messageType() == SERVICE_OPENED) {
                String serviceName = msg.getElementAsString("serviceName");
                logger.info("service has been opened " + serviceName);
            } else {
                throw new IllegalArgumentException("unknown msgType " + msg.messageType());
            }
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    protected void processSubscriptionStatus(Event event, Session session) {

        // To be overwritten by subclasses
    }

    protected void processSubscriptionDataEvent(Event event, Session session) {

        // To be overwritten by subclasses
    }

    protected void processResponseEvent(Event event, Session session) {

        // To be overwritten by subclasses
    }
}
