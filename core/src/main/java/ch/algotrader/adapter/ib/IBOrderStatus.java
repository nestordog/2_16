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
package ch.algotrader.adapter.ib;

import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Status;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IBOrderStatus {

    private final Status status;
    private final long filledQuantity;
    private final long remainingQuantity;
    private final double avgFillPrice;
    private final double lastFillPrice;
    private final String extId;
    private final Order order;
    private final String reason;

    public IBOrderStatus(Status status, long filledQuantity, long remainingQuantity, String extId, Order order) {
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.avgFillPrice = 0.0;
        this.lastFillPrice = 0.0;
        this.extId = extId;
        this.order = order;
        this.reason = null;
    }

    public IBOrderStatus(Status status, long filledQuantity, long remainingQuantity, String extId, Order order, String reason) {
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.avgFillPrice = 0.0;
        this.lastFillPrice = 0.0;
        this.extId = extId;
        this.order = order;
        this.reason = reason;
    }

    public IBOrderStatus(Status status, long filledQuantity, long remainingQuantity, double avgFillPrice, double lastFillPrice, String extId, Order order) {
        this.status = status;
        this.filledQuantity = filledQuantity;
        this.remainingQuantity = remainingQuantity;
        this.avgFillPrice = avgFillPrice;
        this.lastFillPrice = lastFillPrice;
        this.extId = extId;
        this.order = order;
        this.reason = null;
    }

    public Status getStatus() {
        return this.status;
    }

    public long getFilledQuantity() {
        return this.filledQuantity;
    }

    public long getRemainingQuantity() {
        return this.remainingQuantity;
    }

    public double getAvgFillPrice() {
        return this.avgFillPrice;
    }

    public double getLastFillPrice() {
        return this.lastFillPrice;
    }

    public String getExtId() {
        return this.extId;
    }

    public Order getOrder() {
        return this.order;
    }

    public String getReason() {
        return this.reason;
    }
}
