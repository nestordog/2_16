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
package ch.algotrader.util.diff.convert;

import java.util.Objects;

/**
 * Splits a string into substrings and takes only a part of that split.
 */
public class SplitStringConverter extends AbstractValueConverter<String> {

    private final char delimiter;
    private final Mode mode;

    public static enum Mode {
        /** Returns substring before first delimiter occurrence*/
        BEFORE_FIRST,
        /** Returns substring after first delimiter occurrence*/
        AFTER_FIRST,
        /** Returns substring before last delimiter occurrence*/
        BEFORE_LAST,
        /** Returns substring after last delimiter occurrence*/
        AFTER_LAST;
        private int indexOf(String value, char delimiter) {
            if (this == BEFORE_FIRST | this == AFTER_FIRST) {
                return value.indexOf(' ');
            }
            return value.lastIndexOf(' ');
        }

        public String getPart(String value, char delimiter) {
            final int index = indexOf(value, delimiter);
            if (this == BEFORE_FIRST | this == BEFORE_LAST) {
                return index >= 0 ? value.substring(0, index) : value;
            }
            return index >= 0 ? value.substring(index + 1) : "";
        }
    }

    public SplitStringConverter(char delimiter, Mode mode) {
        super(String.class);
        this.delimiter = delimiter;
        this.mode = Objects.requireNonNull(mode, "mode cannot be null");
    }

    @Override
    public String convert(String column, String value) {
        return value != null ? mode.getPart(value, delimiter) : null;
    }
}
