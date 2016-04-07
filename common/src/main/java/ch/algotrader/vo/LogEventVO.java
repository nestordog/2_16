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
package ch.algotrader.vo;

import java.io.Serializable;
import java.util.Locale;

import org.apache.commons.lang.Validate;

public class LogEventVO implements Serializable {

    private static final long serialVersionUID = -6613387966860022782L;

    private final String eventMessage;
    private final String priority;
    private final Class<? extends Throwable> exceptionClass;
    private final String exceptionMessage;

    public LogEventVO(final String priority, final String eventMessage, final Class<? extends Throwable> exceptionClass, final String exceptionMessage) {

        Validate.notEmpty(priority, "Log priority is empty");

        this.priority = priority.toLowerCase(Locale.ROOT);
        this.eventMessage = eventMessage;
        this.exceptionClass = exceptionClass;
        this.exceptionMessage = exceptionMessage;
    }

    public LogEventVO(final String priority, final String eventMessage) {
        this(priority, eventMessage, null, null);
    }

    public LogEventVO(final String priority, final String eventMessage, final Throwable throwable) {
        this(priority, eventMessage, throwable != null ? throwable.getClass() : null, throwable != null ? throwable.getMessage() : null);
    }

    public String getEventMessage() {
        return eventMessage;
    }

    public String getPriority() {
        return priority;
    }

    public Class<? extends Throwable> getExceptionClass() {
        return exceptionClass;
    }

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    @Override
    public String toString() {
        return priority + ": " + eventMessage;
    }

}
