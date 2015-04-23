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
package ch.algotrader.service.bb;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.bloomberglp.blpapi.Element;
import com.bloomberglp.blpapi.Event;
import com.bloomberglp.blpapi.Message;
import com.bloomberglp.blpapi.Request;
import com.bloomberglp.blpapi.Schema.Datatype;
import com.bloomberglp.blpapi.Service;
import com.bloomberglp.blpapi.Session;

import ch.algotrader.adapter.bb.BBAdapter;
import ch.algotrader.adapter.bb.BBConstants;
import ch.algotrader.adapter.bb.BBMessageHandler;
import ch.algotrader.adapter.bb.BBSession;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureDao;
import ch.algotrader.entity.security.FutureFamily;
import ch.algotrader.entity.security.Option;
import ch.algotrader.entity.security.OptionDao;
import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.security.SecurityFamilyDao;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.InitializingServiceType;
import ch.algotrader.enumeration.OptionType;
import ch.algotrader.service.InitializationPriority;
import ch.algotrader.service.InitializingServiceI;
import ch.algotrader.service.ReferenceDataServiceImpl;
import ch.algotrader.util.DateTimeLegacy;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@InitializationPriority(value = InitializingServiceType.BROKER_INTERFACE)
public class BBReferenceDataServiceImpl extends ReferenceDataServiceImpl implements BBReferenceDataService, InitializingServiceI {

    private static final long serialVersionUID = 8938937374871069522L;

    private static final Logger logger = LogManager.getLogger(BBHistoricalDataServiceImpl.class.getName());
    private static BBSession session;

    private final BBAdapter bBAdapter;

    private final SecurityFamilyDao securityFamilyDao;

    private final OptionDao optionDao;

    private final FutureDao futureDao;

    public BBReferenceDataServiceImpl(final BBAdapter bBAdapter,
            final SecurityFamilyDao securityFamilyDao,
            final OptionDao optionDao,
            final FutureDao futureDao) {

        Validate.notNull(bBAdapter, "BBAdapter is null");
        Validate.notNull(securityFamilyDao, "SecurityFamilyDao is null");
        Validate.notNull(optionDao, "OptionDao is null");
        Validate.notNull(futureDao, "FutureDao is null");

        this.bBAdapter = bBAdapter;
        this.securityFamilyDao = securityFamilyDao;
        this.optionDao = optionDao;
        this.futureDao = futureDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init() {

        try {
            session = this.bBAdapter.getReferenceDataSession();
        } catch (IOException ex) {
            throw new BBReferenceDataServiceException(ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new BBReferenceDataServiceException(ex);
        }
    }

    @Override
    public void retrieve(int securityFamilyId) {

        SecurityFamily securityFamily = this.securityFamilyDao.get(securityFamilyId);
        if (securityFamily == null) {
            throw new BBReferenceDataServiceException("securityFamily was not found " + securityFamilyId);
        }

        Security underlying = securityFamily.getUnderlying();
        if (underlying == null) {
            throw new BBReferenceDataServiceException("no underlying defined for  " + securityFamily);
        }

        String bbgid = underlying.getBbgid();
        if (bbgid == null) {
            throw new BBReferenceDataServiceException("no bbgid defined for  " + underlying);
        }

        String securityString = "/bbgid/" + bbgid;

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
            throw new IllegalArgumentException("illegal securityFamily type " + securityFamilyId);
        }

        // send request
        try {
            session.sendRequest(symbolRequest, null);
        } catch (IOException ex) {
            throw new BBReferenceDataServiceException(ex);
        }

        // instantiate the message handler
        BBSymbolHandler symbolHandler = new BBSymbolHandler();

        // process responses
        boolean done = false;
        while (!done) {
            try {
                done = symbolHandler.processEvent(session);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new BBReferenceDataServiceException(ex);
            }
        }

        List<String> symbols = symbolHandler.getSymbols();

        if (symbols.isEmpty()) {
            throw new IllegalArgumentException("securityFamily does not contain a chain " + securityFamilyId);
        }

        // security request
        Request securityRequest = service.createRequest("ReferenceDataRequest");

        // Add securities to request
        for (String symbol : symbols) {
            securityRequest.append("securities", symbol);
        }

        securityRequest.append("fields", "ID_BB_GLOBAL");
        securityRequest.append("fields", "ID_BB_SEC_NUM_DES");
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
        try {
            session.sendRequest(securityRequest, null);
        } catch (IOException ex) {
            throw new BBReferenceDataServiceException(ex);
        }

        // instantiate the message handler
        BBSecurityHandler securityHandler = new BBSecurityHandler(securityFamily);

        // process responses
        done = false;
        while (!done) {
            try {
                done = securityHandler.processEvent(session);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                throw new BBReferenceDataServiceException(ex);
            }
        }

        // store all new securites in the database
        securityHandler.store();

    }

    @Override
    public void retrieveStocks(int securityFamilyId, String symbol) {

        throw new UnsupportedOperationException("not implemented yet");
    }

    private class BBSymbolHandler extends BBMessageHandler {

        private final List<String> symbols = new ArrayList<>();

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

            if (securityData.hasElement(BBConstants.FIELD_EXCEPTIONS)) {

                Element fieldExceptions = securityData.getElement(BBConstants.FIELD_EXCEPTIONS);
                if (fieldExceptions.numValues() > 0) {
                    Element fieldException = fieldExceptions.getValueAsElement(0);
                    throw new IllegalArgumentException(fieldException.getElement(BBConstants.ERROR_INFO).toString());
                }
            }

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

            Comparator<Security> comparator = (o1, o2) -> o1.getBbgid().compareTo(o2.getBbgid());

            this.existingSecurities = new TreeSet<>(comparator);

            if (securityFamily instanceof OptionFamily) {
                this.existingSecurities.addAll(BBReferenceDataServiceImpl.this.optionDao.findBySecurityFamily(this.securityFamily.getId()));
            } else if (securityFamily instanceof FutureFamily) {
                this.existingSecurities.addAll(BBReferenceDataServiceImpl.this.futureDao.findBySecurityFamily(this.securityFamily.getId()));
            } else {
                throw new IllegalArgumentException("illegal securityFamily type");
            }

            this.newFutures = new TreeSet<>(comparator);
            this.newOptions = new TreeSet<>(comparator);
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
                        throw new BBReferenceDataServiceException(e);
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

                if (securityData.hasElement(BBConstants.FIELD_EXCEPTIONS)) {

                    Element fieldExceptions = securityData.getElement(BBConstants.FIELD_EXCEPTIONS);
                    if (fieldExceptions.numValues() > 0) {
                        Element fieldException = fieldExceptions.getValueAsElement(0);
                        throw new IllegalArgumentException(fieldException.getElement(BBConstants.ERROR_INFO).toString());
                    }
                }

                if (!securityData.hasElement(BBConstants.FIELD_DATA)) {
                    throw new IllegalStateException("need field data");
                }

                Element fields = securityData.getElement(BBConstants.FIELD_DATA);

                String symbol = fields.getElementAsString(BBConstants.ID_BB_SEC_NUM_DES);
                String sector = fields.getElementAsString(BBConstants.MARKET_SECTOR_DES);
                String bbgid = fields.getElementAsString(BBConstants.ID_BB_GLOBAL);
                String currencyString = fields.getElementAsString(BBConstants.CRNCY);
                Currency currency = Currency.valueOf(currencyString);

                // ignore securities with different currencies than the securityFamily
                if (!(currency.equals(this.securityFamily.getCurrency()))) {
                    logger.warn(symbol + " difference in currency, db: " + this.securityFamily.getCurrency() + " bb: " + currency);
                }

                if (this.securityFamily instanceof OptionFamily) {

                    int contractSize = fields.getElementAsInt32(BBConstants.OPT_CONT_SIZE);

                    // ignore securities with different contractSize than the securityFamily
                    if (this.securityFamily.getContractSize(Broker.BBG) != contractSize) {
                        logger.warn(symbol + " difference in contract size, db: " + this.securityFamily.getContractSize(Broker.BBG) + " bb: " + contractSize);
                    }

                    String expirationString = fields.getElementAsString(BBConstants.OPT_EXPIRE_DT);
                    double strikeDouble = fields.getElementAsFloat64(BBConstants.OPT_STRIKE_PX);
                    String typeString = fields.getElementAsString(BBConstants.OPT_PUT_CALL);

                    Date expiration = DateTimeLegacy.parseAsDateTimeGMT(expirationString);
                    BigDecimal strike = RoundUtil.getBigDecimal(strikeDouble, this.securityFamily.getScale(Broker.BBG));
                    OptionType type = OptionType.valueOf(typeString.toUpperCase());

                    Option option = Option.Factory.newInstance();

                    option.setSymbol(symbol + " " + sector);
                    option.setBbgid(bbgid);
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
                    if (this.securityFamily.getContractSize(Broker.BBG) != contractSize) {
                        logger.warn(symbol + " difference in contract size, db: " + this.securityFamily.getContractSize(Broker.BBG) + " bb: " + contractSize);
                    }

                    String lastTradingString = fields.getElementAsString(BBConstants.LAST_TRADEABLE_DT);
                    String firstNoticeString = fields.getElementAsString(BBConstants.FUT_NOTICE_FIRST);

                    Date lastTrading = DateTimeLegacy.parseAsDateTimeGMT(lastTradingString);
                    Date firstNotice = DateTimeLegacy.parseAsDateTimeGMT(firstNoticeString);

                    Future future = Future.Factory.newInstance();

                    future.setSymbol(symbol + " " + sector);
                    future.setBbgid(bbgid);
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

                BBReferenceDataServiceImpl.this.optionDao.saveAll(this.newOptions);
                logger.debug("retrieved options for optionFamily: " + this.securityFamily.getName() + " " + this.newOptions);

            } else if (this.securityFamily instanceof FutureFamily) {

                BBReferenceDataServiceImpl.this.futureDao.saveAll(this.newFutures);
                logger.debug("retrieved futures for futureFamily: " + this.securityFamily.getName() + " " + this.newFutures);

            } else {
                throw new IllegalArgumentException("illegal securityFamily type");
            }
        }
    }
}
