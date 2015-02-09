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
package ch.algotrader.entity.security;

import java.math.BigDecimal;
import java.text.ChoiceFormat;
import java.util.Objects;

import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.enumeration.Broker;
import ch.algotrader.util.RoundUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SecurityFamilyImpl extends SecurityFamily {

    private static final long serialVersionUID = -2318908709333325986L;

    @Override
    public String toString() {

        return getName();
    }

    @Override
    public BigDecimal getTickSize(BigDecimal price, boolean upwards) {

        return RoundUtil.getBigDecimal(getTickSize(price.doubleValue(), upwards), getScale());
    }

    @Override
    public double getTickSize(double price, boolean upwards) {

        // add or subtract a very small amount to the price to get the tickSize just above or below the trigger
        double adjustedPrice = upwards ? price * 1.00000000001 : price / 1.00000000001;
        return Double.valueOf(new ChoiceFormat(getTickSizePattern()).format(adjustedPrice));
    }


    @Override
    public BigDecimal getTotalCharges() {

        BigDecimal totalCharges = new BigDecimal(0.0);

        if (getExecutionCommission() != null) {
            totalCharges.add(getExecutionCommission());
        }

        if (getClearingCommission() != null) {
            totalCharges.add(getExecutionCommission());
        }

        if (getFee() != null) {
            totalCharges.add(getFee());
        }

        return totalCharges;
    }

    @Override
    public String getSymbolRoot(Broker broker) {

        BrokerParameters brokerParams = getBrokerParameters().get(broker.toString());
        if (brokerParams != null && brokerParams.getSymbolRoot() != null) {
            return brokerParams.getSymbolRoot();
        } else {
            return getSymbolRoot();
        }
    }

    @Override
    public String getExchangeCode(Broker broker) {

        BrokerParameters brokerParams = getBrokerParameters().get(broker.toString());
        if (brokerParams != null && brokerParams.getExchangeCode() != null) {
            return brokerParams.getExchangeCode();
        } else {
            Exchange exchange = getExchange();
            return exchange != null ? exchange.getCode() : null;
        }
    }

    @Override
    public BigDecimal getExecutionCommission(Broker broker) {

        BrokerParameters brokerParams = getBrokerParameters().get(broker.toString());
        if (brokerParams != null && brokerParams.getExecutionCommission() != null) {
            return brokerParams.getExecutionCommission();
        } else {
            return getExecutionCommission();
        }
    }

    @Override
    public BigDecimal getClearingCommission(Broker broker) {

        BrokerParameters brokerParams = getBrokerParameters().get(broker.toString());
        if (brokerParams != null && brokerParams.getClearingCommission() != null) {
            return brokerParams.getClearingCommission();
        } else {
            return getClearingCommission();
        }
    }

    @Override
    public BigDecimal getFee(Broker broker) {

        BrokerParameters brokerParams = getBrokerParameters().get(broker.toString());
        if (brokerParams != null && brokerParams.getFee() != null) {
            return brokerParams.getFee();
        } else {
            return getFee();
        }
    }

    @Override
    public BigDecimal getTotalCharges(Broker broker) {

        BigDecimal totalCharges = new BigDecimal(0.0);

        if (getExecutionCommission(broker) != null) {
            totalCharges.add(getExecutionCommission(broker));
        }

        if (getClearingCommission(broker) != null) {
            totalCharges.add(getExecutionCommission(broker));
        }

        if (getFee(broker) != null) {
            totalCharges.add(getFee(broker));
        }

        return totalCharges;
    }

    @Override
    public int getSpreadTicks(BigDecimal bid, BigDecimal ask) {

        int ticks = 0;
        BigDecimal price = bid;
        if (bid.compareTo(ask) <= 0) {
            while (price.compareTo(ask) < 0) {
                ticks++;
                price = adjustPrice(price, 1);
            }
        } else {
            while (price.compareTo(ask) > 0) {
                ticks--;
                price = adjustPrice(price, -1);
            }
        }
        return ticks;
    }

    @Override
    public BigDecimal adjustPrice(BigDecimal price, int ticks) {

        if (ticks > 0) {
            for (int i = 0; i < ticks; i++) {
                price = price.add(getTickSize(price, true));
            }
        } else if (ticks < 0) {
            for (int i = 0; i > ticks; i--) {
                price = price.subtract(getTickSize(price, false));
            }
        }
        return price;
    }

    @Override
    public double adjustPrice(double price, int ticks) {

        if (ticks > 0) {
            for (int i = 0; i < ticks; i++) {
                price = price + getTickSize(price, true);
            }
        } else if (ticks < 0) {
            for (int i = 0; i > ticks; i--) {
                price = price - getTickSize(price, false);
            }
        }
        return price;
    }

    @Override
    public BigDecimal roundUp(BigDecimal price) {
        return RoundUtil.roundToNextN(price, getTickSize(price, true).doubleValue(), BigDecimal.ROUND_UP);
    }

    @Override
    public double roundUp(double price) {
        return RoundUtil.roundToNextN(price, getTickSize(price, true), BigDecimal.ROUND_UP);
    }

    @Override
    public BigDecimal roundDown(BigDecimal price) {
        return RoundUtil.roundToNextN(price, getTickSize(price, false).doubleValue(), BigDecimal.ROUND_DOWN);
    }

    @Override
    public double roundDown(double price) {
        return RoundUtil.roundToNextN(price, getTickSize(price, false), BigDecimal.ROUND_DOWN);
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof SecurityFamily) {
            SecurityFamily that = (SecurityFamily) obj;
            return Objects.equals(this.getName(), that.getName());

        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(getName());
        return hash;
    }
}
