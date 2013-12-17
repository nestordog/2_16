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

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionImpl;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.future.FutureSymbol;
import ch.algotrader.option.OptionSymbol;
import ch.algotrader.util.MyLogger;
import ch.algotrader.util.RoundUtil;

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
    private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private static Logger logger = MyLogger.getLogger(BBHistoricalDataServiceImpl.class.getName());
    private static BBSession session;


    @Override
    protected void handleInit() throws Exception {

        session = getBBSessionFactory().getReferenceDataSession();
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

        if (securityFamily instanceof OptionFamily) {
            symbolRequest.append("fields", "OPT_CHAIN");
        } else if (securityFamily instanceof FutureFamily) {
            symbolRequest.append("fields", "FUT_CHAIN");
        } else {
            throw new IllegalArgumentException("illegal securityFamily type");
        }

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

        securityRequest.append("fields", "ID_BB_GLOBAL");
        securityRequest.append("fields", "TICKER");
        securityRequest.append("fields", "CRNCY");

        if (securityFamily instanceof OptionFamily) {
            securityRequest.append("fields", "OPT_STRIKE_PX");
            securityRequest.append("fields", "OPT_EXPIRE_DT");
            securityRequest.append("fields", "OPT_PUT_CALL");
            securityRequest.append("fields", "OPT_CONT_SIZE");

        } else if (securityFamily instanceof FutureFamily) {
            securityRequest.append("fields", "LAST_TRADEABLE_DT");
            securityRequest.append("fields", "FUT_NOTICE_FIRST");
            securityRequest.append("fields", "FUT_CONT_SIZE");
        } else {
            throw new IllegalArgumentException("illegal securityFamily type");
        }

        // send request
        session.sendRequest(securityRequest, null);

        // instantiate the message handler
        BBSecurityHandler securityHandler = new BBSecurityHandler(securityFamily);

        // process responses
        done = false;
        while (!done) {
            done = securityHandler.processEvent(session);
        }

        // store all new securites in the database
        securityHandler.store();
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

            Element securitiesData = msg.getElement(BBConstants.SECURITY_DATA);

            if (securitiesData.numValues() == 0 || securitiesData.numValues() > 1) {
                throw new IllegalStateException("expected one field");
            }

            Element securityData = securitiesData.getValueAsElement(0);

            if (!securityData.hasElement(BBConstants.FIELD_DATA)) {
                throw new IllegalStateException("need field data");
            }

            Element fields = securityData.getElement(BBConstants.FIELD_DATA);

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
        private final Set<Security> existingSecurities;
        private final Set<Future> newFutures;
        private final Set<Option> newOptions;

        public BBSecurityHandler(SecurityFamily securityFamily) {

            this.securityFamily = securityFamily;

            Comparator<Security> comparator = new Comparator<Security>() {
                @Override
                public int compare(Security o1, Security o2) {
                    return o1.getBbgid().compareTo(o2.getBbgid());
                }
            };

            this.existingSecurities = new TreeSet<Security>(comparator);

            if (securityFamily instanceof OptionFamily) {
                this.existingSecurities.addAll(getOptionDao().findBySecurityFamily(this.securityFamily.getId()));
            } else if (securityFamily instanceof FutureFamily) {
                this.existingSecurities.addAll(getFutureDao().findBySecurityFamily(this.securityFamily.getId()));
            } else {
                throw new IllegalArgumentException("illegal securityFamily type");
            }

            this.newFutures = new TreeSet<Future>(comparator);
            this.newOptions = new TreeSet<Option>(comparator);
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
                    try {
                        processReferenceDataResponse(msg);
                    } catch (ParseException e) {
                        throw new BBSecurityRetrieverServiceException(e);
                    }
                } else {
                    throw new IllegalArgumentException("unknown reponse type: " + msg.messageType());
                }
            }
        }

        private void processReferenceDataResponse(Message msg) throws ParseException {

            Element securitiesData = msg.getElement(BBConstants.SECURITY_DATA);
            for (int i = 0; i < securitiesData.numValues(); ++i) {

                Element securityData = securitiesData.getValueAsElement(i);

                Element fields = securityData.getElement(BBConstants.FIELD_DATA);

                String symbol = fields.getElementAsString(BBConstants.TICKER);
                String bbgid = fields.getElementAsString(BBConstants.ID_BB_GLOBAL);
                String currencyString = fields.getElementAsString(BBConstants.CRNCY);
                Currency currency = Currency.fromString(currencyString);

                // ignore securities with different currencies than the securityFamily
                if (!(currency.equals(this.securityFamily.getCurrency()))) {
                    continue;
                }

                if (this.securityFamily instanceof OptionFamily) {

                    int contractSize = fields.getElementAsInt32(BBConstants.OPT_CONT_SIZE);

                    // ignore securities with different contractSize than the securityFamily
                    if (this.securityFamily.getContractSize() != contractSize) {
                        continue;
                    }

                    String expirationString = fields.getElementAsString(BBConstants.OPT_EXPIRE_DT);
                    double strikeDouble = fields.getElementAsFloat64(BBConstants.OPT_STRIKE_PX);
                    String typeString = fields.getElementAsString(BBConstants.OPT_PUT_CALL);

                    Date expiration = format.parse(expirationString);
                    BigDecimal strike = RoundUtil.getBigDecimal(strikeDouble, this.securityFamily.getScale());
                    OptionType type = OptionType.fromString(typeString.toUpperCase());

                    String isin = OptionSymbol.getIsin(this.securityFamily, expiration, type, strike);
                    String ric = OptionSymbol.getRic(this.securityFamily, expiration, type, strike);

                    Option option = new OptionImpl();

                    option.setSymbol(symbol);
                    option.setBbgid(bbgid);
                    option.setIsin(isin);
                    option.setRic(ric);
                    option.setSecurityFamily(this.securityFamily);
                    option.setUnderlying(this.securityFamily.getUnderlying());

                    option.setExpiration(expiration);
                    option.setStrike(strike);
                    option.setType(type);

                    // ignore options that already exist
                    if (!this.existingSecurities.contains(option)) {
                        this.newOptions.add(option);
                    }

                } else if (this.securityFamily instanceof FutureFamily) {

                    int contractSize = fields.getElementAsInt32(BBConstants.FUT_CONT_SIZE);

                    // ignore securities with different contractSize than the securityFamily
                    if (this.securityFamily.getContractSize() != contractSize) {
                        continue;
                    }

                    String lastTradingString = fields.getElementAsString(BBConstants.LAST_TRADEABLE_DT);
                    String firstNoticeString = fields.getElementAsString(BBConstants.FUT_NOTICE_FIRST);

                    Date lastTrading = format.parse(lastTradingString);
                    Date firstNotice = format.parse(firstNoticeString);

                    String isin = FutureSymbol.getIsin(this.securityFamily, lastTrading);
                    String ric = FutureSymbol.getRic(this.securityFamily, lastTrading);

                    Future future = new FutureImpl();

                    future.setSymbol(symbol);
                    future.setBbgid(bbgid);
                    future.setIsin(isin);
                    future.setRic(ric);
                    future.setSecurityFamily(this.securityFamily);
                    future.setUnderlying(this.securityFamily.getUnderlying());

                    future.setExpiration(lastTrading);
                    future.setLastTrading(lastTrading);
                    future.setFirstNotice(firstNotice);

                    // ignore futures that already exist
                    if (!this.existingSecurities.contains(future)) {
                        this.newFutures.add(future);
                    }

                } else {
                    throw new IllegalArgumentException("illegal securityFamily type");
                }
            }
        }

        public void store() {

            if (this.securityFamily instanceof OptionFamily) {
                getOptionDao().create(this.newOptions);
            } else if (this.securityFamily instanceof FutureFamily) {
                getFutureDao().create(this.newFutures);
            } else {
                throw new IllegalArgumentException("illegal securityFamily type");
            }

            logger.debug("retrieved securities for securityFamily: " + this.securityFamily.getName() + " " + this.newFutures);
        }
    }
}
