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
package ch.algotrader.util.diff;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Result returned after a successful DIFF run.
 */
public class DiffStats {

    private final int expLinesRead;
    private final int actLinesRead;
    private final int linesCompared;

    public DiffStats(int expLinesRead, int actLinesRead, int linesCompared) {
        this.expLinesRead = expLinesRead;
        this.actLinesRead = actLinesRead;
        this.linesCompared = linesCompared;
    }

    public int getExpLinesRead() {
        return expLinesRead;
    }
    public int getActLinesRead() {
        return actLinesRead;
    }
    public int getLinesCompared() {
        return linesCompared;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
