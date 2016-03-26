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
package ch.algotrader.util;

import java.math.BigDecimal;

import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.entity.trade.Order;
import ch.algotrader.enumeration.Side;


/**
 * Provides price normalization methods
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class PriceUtil {

    /**
     * normalizes a price from a broker specific basis.
     *
     *   <ol>
     *   <li>divides the price by the defined price multiplier</li>
     *   <li>sets the scale of the normalized price by using the default scale (not the broker-specific one></li>
     *   </ol>
     */
    public static BigDecimal normalizePrice(final Order order, double price) {

        return normalizePrice(order.getSecurity().getSecurityFamily(), order.getAccount().getBroker(), price);
    }

    /**
     * normalizes a price from a broker specific basis.
     *
     *   <ol>
     *   <li>divides the price by the defined price multiplier</li>
     *   <li>sets the scale of the normalized price by using the default scale (not the broker-specific one></li>
     *   </ol>
     */
    public static BigDecimal normalizePrice(SecurityFamily securityFamily, String broker, double price) {

        double normalizedPrice = price / securityFamily.getPriceMultiplier(broker);

        // use the default scale (not the broker specific one)
        return RoundUtil.getBigDecimal(normalizedPrice, securityFamily.getScale());
    }

    /**
     * de-normalizes a price to a broker specific basis.
     *
     *   <ol>
     *   <li>multiplies the price by the defined price multiplier</li>
     *   <li>rounds the price to next tick. Buy orders are rounded down, SELL orders are rounded up, BUY orders are rounded down</li>
     *   </ol>
     */
    public static double denormalizePrice(final Order order, final BigDecimal price) {

        return denormalizePrice(order.getSecurity().getSecurityFamily(), order.getAccount().getBroker(), order.getSide() == Side.SELL, price);
    }

    /**
     * de-normalizes a price to a broker specific basis.
     *
     *   <ol>
     *   <li>multiplies the price by the defined price multiplier</li>
     *   <li>rounds the price to next tick either up or down. </li>
     *   </ol>
     */
    public static double denormalizePrice(SecurityFamily securityFamily, String broker, boolean roundUp, final BigDecimal price) {

        double denormalizedPrice = price.doubleValue() * securityFamily.getPriceMultiplier(broker);
        if (roundUp) {
            return securityFamily.roundUp(broker, denormalizedPrice);
        } else {
            return securityFamily.roundDown(broker, denormalizedPrice);
        }
    }

}
