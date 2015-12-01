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

public class InternalErrorVO implements Serializable {

    private static final long serialVersionUID = -4213892824596370869L;

    private final Class<? extends Exception> exceptionClass;
    private final String message;

    public InternalErrorVO(Class<? extends Exception> exceptionClass, String message) {
        this.exceptionClass = exceptionClass;
        this.message = message;
    }

    public Class<? extends Exception> getExceptionClass() {
        return exceptionClass;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return exceptionClass + ": " + message;
    }

}
