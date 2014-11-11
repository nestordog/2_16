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
package ch.algotrader.util.diff.value;

/**
 * Value asserter for strings converting the string to UPPER or LOWER case before
 * the assertion.
 */
public class CaseInsensitiveStringAsserter extends AbstractValueAsserter<String> {

    public static final CaseInsensitiveStringAsserter TO_UPPER_CASE = new CaseInsensitiveStringAsserter(true);
    public static final CaseInsensitiveStringAsserter TO_LOWER_CASE = new CaseInsensitiveStringAsserter(false);

    private boolean toUpperCase;
    private CaseInsensitiveStringAsserter(boolean toUpperCase) {
        super(String.class);
        this.toUpperCase = toUpperCase;
    }

    @Override
    public String convert(String column, String value) {
        return value == null ? null : toUpperCase ? value.toUpperCase() : value.toLowerCase();
    }
}
