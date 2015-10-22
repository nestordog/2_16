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
package ch.algotrader.entity.trade;

import org.apache.commons.lang.ClassUtils;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.enumeration.OrderPropertyType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public abstract class OrderImpl extends Order {

    private static final long serialVersionUID = -6501807818853981164L;

    @Override
    public void setQuantity(long quantityIn) {

        // always set a positive quantity
        super.setQuantity(Math.abs(quantityIn));
    }

    @Override
    public String getRootIntId() {

        // for FIX Orders remove the Order Version
        if (getIntId() != null && getIntId().contains(".")) {
            return getIntId().split("\\.")[0];
        } else {
            return getIntId();
        }
    }

    @Override
    public String getDescription() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getSide());
        buffer.append(",");
        buffer.append(getQuantity());
        buffer.append(",");
        buffer.append(ClassUtils.getShortClassName(this.getClass()));
        buffer.append(",");
        buffer.append(getSecurity());
        buffer.append(",");
        buffer.append(getStrategy());

        if (getTif() != null) {
            buffer.append(",tif=");
            buffer.append(getTif());
        }

        if (getIntId() != null) {
            buffer.append(",intId=");
            buffer.append(getIntId());
        }

        if (getAccount() != null) {
            buffer.append(",account=");
            buffer.append(getAccount());
        }

        if (getExchange() != null) {
            buffer.append(",exchange=");
            buffer.append(getExchange());
        }

        return buffer.toString();
    }

    @Override
    public Exchange getEffectiveExchange() {

        if (getExchange() != null) {
            return getExchange();
        } else {
            return getSecurity().getSecurityFamily().getExchange();
        }
    }

    @Override
    public void addProperty(String name, String value, OrderPropertyType type) {

        getOrderProperties().put(name, OrderProperty.Factory.newInstance(name, type, value, this));
    }

    @Override
    public String getProperty(String name) {

        return getOrderProperties().get(name).getValue();
    }

    @Override
    public String toString() {

        StringBuilder buffer = new StringBuilder();

        buffer.append(getDescription());

        if (!"".equals(getExtDescription())) {
            buffer.append(",");
            buffer.append(getExtDescription());
        }

        return buffer.toString();
    }

}
