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
package ch.algotrader.service.bb;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.algotrader.adapter.bb.BBConstants;
import ch.algotrader.adapter.bb.BBMessageHandler;
import ch.algotrader.adapter.bb.BBSession;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.util.MyLogger;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Schema.Datatype;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BBSecurityRetrieverServiceImpl extends BBSecurityRetrieverServiceBase {

    private static final long serialVersionUID = 8938937374871069522L;

    private static Logger logger = MyLogger.getLogger(BBHistoricalDataServiceImpl.class.getName());
    private static BBSession session;

    @Override
    protected void handleInit() throws Exception {

        session = getBBSessionFactory().getMarketDataSession();
    }

    @Override
    protected void handleRetrieve(int securityFamilyId) throws Exception {

        SecurityFamily securityFamily = getSecurityFamilyDao().get(securityFamilyId);
        if (securityFamily == null) {
            throw new BBSecurityRetrieverServiceException("securityFamily was not found " + securityFamilyId);
        }

        String securityString = "/bbgid/" + securityFamily.getUnderlying().getBbgid();

        Service service = session.getService();

        // symbol request
        Request symbolRequest = service.createRequest("ReferenceDataRequest");

        // Add securities to request
        symbolRequest.append("securities", securityString);
        symbolRequest.append("fields", "FUT_CHAIN");

        // send request
        session.sendRequest(symbolRequest, null);

        // instantiate the message handler
        BBSymbolHandler symbolHandler = new BBSymbolHandler();

        // process responses
        boolean done = false;
        while (!done) {
            done = symbolHandler.processEvent(session);
        }

        List<String> symbols = symbolHandler.getSymbols();

        // security request
        Request securityRequest = service.createRequest("ReferenceDataRequest");

        // Add securities to request
        for (String symbol : symbols) {
            securityRequest.append("securities", symbol);
        }

        securityRequest.append("fields", "BB_ID_GLOBAL");

        // send request
        session.sendRequest(securityRequest, null);

        // instantiate the message handler
        BBSecurityHandler securityHandler = new BBSecurityHandler(securityFamily);

        // process responses
        done = false;
        while (!done) {
            done = securityHandler.processEvent(session);
        }
    }

    @Override
    protected void handleRetrieveStocks(int securityFamilyId, String symbol) throws Exception {

        throw new UnsupportedOperationException("not implemented yet");
    }

    private class BBSymbolHandler extends BBMessageHandler {

        private final List<String> symbols = new ArrayList<String>();

        @Override
        protected void processResponseEvent(Event event, Session session) {

            for (Message msg : event) {

                if (msg.hasElement(BBConstants.RESPONSE_ERROR)) {

                    Element errorInfo = msg.getElement(BBConstants.RESPONSE_ERROR);
                    logger.error("request failed " + errorInfo.getElementAsString(BBConstants.CATEGORY) + " (" + errorInfo.getElementAsString(BBConstants.MESSAGE) + ")");

                    continue;
                }

                if (msg.messageType() == BBConstants.REFERENCE_DATA_RESPONSE) {
                    processReferenceDataResponse(msg);
                } else {
                    throw new IllegalArgumentException("unknown reponse type: " + msg.messageType());
                }
            }
        }

        private void processReferenceDataResponse(Message msg) {

            Element security = msg.getElement(BBConstants.SECURITY_DATA);
            Element fields = security.getElement(BBConstants.FIELD_DATA);

            if (fields.numElements() == 0 || fields.numElements() > 1) {
                throw new IllegalStateException("expected one field");
            }

            Element field = fields.getElement(0);

            if (field.datatype() != Datatype.SEQUENCE) {
                throw new IllegalStateException("expected field type sequence");
            }

            for (int i = 0; i < field.numValues(); i++) {

                Element bulkElement = field.getValueAsElement(i);

                if (bulkElement.numElements() != 1) {
                    throw new IllegalStateException("expected one element inside bulkElement");
                }

                Element element = bulkElement.getElement(0);
                String symbol = element.getValueAsString();
                this.symbols.add(symbol);
            }
        }

        public List<String> getSymbols() {

            return this.symbols;
        }
    }

    private class BBSecurityHandler extends BBMessageHandler {

        private final SecurityFamily securityFamily;

        public BBSecurityHandler(SecurityFamily securityFamily) {

            this.securityFamily = securityFamily;
        }

        @Override
        protected void processResponseEvent(Event event, Session session) {

            for (Message msg : event) {

                if (msg.hasElement(BBConstants.RESPONSE_ERROR)) {

                    Element errorInfo = msg.getElement(BBConstants.RESPONSE_ERROR);
                    logger.error("request failed " + errorInfo.getElementAsString(BBConstants.CATEGORY) + " (" + errorInfo.getElementAsString(BBConstants.MESSAGE) + ")");

                    continue;
                }

                if (msg.messageType() == BBConstants.REFERENCE_DATA_RESPONSE) {
                    processReferenceDataResponse(msg);
                } else {
                    throw new IllegalArgumentException("unknown reponse type: " + msg.messageType());
                }
            }
        }

        private void processReferenceDataResponse(Message msg) {

            Element security = msg.getElement(BBConstants.SECURITY_DATA);
            Element fields = security.getElement(BBConstants.FIELD_DATA);

            if (fields.numElements() == 0 || fields.numElements() > 1) {
                throw new IllegalStateException("expected one field");
            }

            Element field = fields.getElement(0);

            Comparator<Security> comparator = new Comparator<Security>() {
                @Override
                public int compare(Security o1, Security o2) {
                    return o1.getBbgid().compareTo(o2.getBbgid());
                }
            };

            Set<Future> existingFutures = new TreeSet<Future>(comparator);
            existingFutures.addAll(getFutureDao().findFuturesBySecurityFamily(this.securityFamily.getId()));
            Set<Future> newFutures = new TreeSet<Future>();

            // get all current futures
            int numBars = field.numValues();
            for (int i = 0; i < numBars; ++i) {

                Element fields2 = field.getValueAsElement(i);

                Future future = new FutureImpl();

                Date expiration = null;// TODO
                String symbol = null; // TODO
                String isin = null; // TODO
                String bbgid = null; // TODO

                String ric = FutureSymbol.getRic(this.securityFamily, expiration);

                future.setSymbol(symbol);
                future.setIsin(isin);
                future.setRic(ric);
                future.setBbgid(bbgid);
                future.setExpiration(expiration);
                future.setSecurityFamily(this.securityFamily);
                future.setUnderlying(this.securityFamily.getUnderlying());

                // ignore futures that already exist
                if (!existingFutures.contains(future)) {
                    newFutures.add(future);
                }
            }

            getFutureDao().create(newFutures);

            logger.debug("retrieved futures for futurefamily: " + this.securityFamily.getName() + " " + newFutures);
        }
    }
}
