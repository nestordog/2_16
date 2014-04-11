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
package ch.algotrader.entity.trade;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OrderCompletionImpl extends OrderCompletion {

    private static final long serialVersionUID = 8510334378552535810L;

    @Override
    public String toString() {

        StringBuffer buffer = new StringBuffer();

        buffer.append(getStatus());

        if (getOrder() != null) {
            buffer.append(",");
            buffer.append(getOrder().getDescription());
        }

        buffer.append(",filledQuantity=");
        buffer.append(getFilledQuantity());
        buffer.append(",remainingQuantity=");
        buffer.append(getRemainingQuantity());
        buffer.append(",avgPrice=");
        buffer.append(getAvgPrice());
        buffer.append(",grossValue=");
        buffer.append(getGrossValue());
        buffer.append(",netValue=");
        buffer.append(getNetValue());
        buffer.append(",totalCharges=");
        buffer.append(getTotalCharges());
        buffer.append(",fills=");
        buffer.append(getFills());
        buffer.append(",executionTime=");
        buffer.append(getExecutionTime());

        return buffer.toString();
    }

}
