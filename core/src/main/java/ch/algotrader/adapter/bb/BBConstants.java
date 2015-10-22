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
package ch.algotrader.adapter.bb;

import com.bloomberglp.blpapi.Name;

/**
 * Contains Bloomberg constants.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class BBConstants {

    public static final Name SESSION_CONNECTION_UP = Name.getName("SessionConnectionUp");
    public static final Name SESSION_CONNECTION_DOWN = Name.getName("SessionConnectionDown");
    public static final Name SESSION_STARTED = Name.getName("SessionStarted");
    public static final Name SESSION_TERMINATED = Name.getName("SessionTerminated");
    public static final Name SESSION_STARTUP_FAILURE = Name.getName("SessionStartupFailure");
    public static final Name SERVICE_OPENED = Name.getName("ServiceOpened");

    public static final Name SUBSCRIPTION_STARTED = Name.getName("SubscriptionStarted");
    public static final Name SUBSCRIPTION_FAILURE = Name.getName("SubscriptionFailure");
    public static final Name SUBSCRIPTION_TERMINATED = Name.getName("SubscriptionTerminated");

    public static final Name RESPONSE_ERROR = Name.getName("responseError");
    public static final Name CATEGORY = Name.getName("category");
    public static final Name MESSAGE = Name.getName("message");

    public static final Name BAR_DATA = Name.getName("barData");
    public static final Name TICK_DATA = Name.getName("tickData");
    public static final Name BAR_TICK_DATA = Name.getName("barTickData");
    public static final Name TICK_TICK_DATA = Name.getName("tickData");
    public static final Name SECURITY_DATA = Name.getName("securityData");
    public static final Name FIELD_DATA = Name.getName("fieldData");
    public static final Name FIELD_EXCEPTIONS = new Name("fieldExceptions");
    public static final Name FIELD_ID = new Name("fieldId");
    public static final Name ERROR_INFO = new Name("errorInfo");

    public static final Name TIME = Name.getName("time");
    public static final Name DATE = Name.getName("date");
    public static final Name TYPE = Name.getName("type");
    public static final Name VALUE = Name.getName("value");
    public static final Name OPEN = Name.getName("open");
    public static final Name HIGH = Name.getName("high");
    public static final Name LOW = Name.getName("low");
    public static final Name CLOSE = Name.getName("low");
    public static final Name VOLUME = Name.getName("volume");
    public static final Name SIZE = Name.getName("size");

    public static final Name HISTORICAL_DATA_RESPONSE = Name.getName("HistoricalDataResponse");
    public static final Name INTRADAY_BAR_RESPONSE = Name.getName("IntradayBarResponse");
    public static final Name INTRADAY_TICK_RESPONSE = Name.getName("IntradayTickResponse");
    public static final Name REFERENCE_DATA_RESPONSE = Name.getName("ReferenceDataResponse");

    public static final Name ID_BB_GLOBAL = Name.getName("ID_BB_GLOBAL");
    public static final Name ID_BB_SEC_NUM_DES = Name.getName("ID_BB_SEC_NUM_DES");
    public static final Name MARKET_SECTOR_DES = Name.getName("MARKET_SECTOR_DES");

    public static final Name CRNCY = Name.getName("CRNCY");

    public static final Name FUT_CONTRACT_DT = Name.getName("FUT_CONTRACT_DT");
    public static final Name LAST_TRADEABLE_DT = Name.getName("LAST_TRADEABLE_DT");
    public static final Name FUT_NOTICE_FIRST = Name.getName("FUT_NOTICE_FIRST");
    public static final Name FUT_CONT_SIZE = Name.getName("FUT_CONT_SIZE");

    public static final Name OPT_EXPIRE_DT = Name.getName("OPT_EXPIRE_DT");
    public static final Name OPT_STRIKE_PX = Name.getName("OPT_STRIKE_PX");
    public static final Name OPT_PUT_CALL = Name.getName("OPT_PUT_CALL");
    public static final Name OPT_CONT_SIZE = Name.getName("OPT_CONT_SIZE");

}
