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
package ch.algotrader.util;

import org.apache.commons.math.util.MathUtils;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.Transaction;
import ch.algotrader.enumeration.Direction;
import ch.algotrader.vo.TradePerformanceVO;

/**
 * Provides different Position related utility methods.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PositionUtil {

    /**
     * process a transaction for a security and strategy where no prior position existed
     */
    public static Position processFirstTransaction(Transaction transaction) {

        double cost = -transaction.getNetValueDouble();

        Position position = Position.Factory.newInstance();

        position.setSecurity(transaction.getSecurity());
        position.setStrategy(transaction.getStrategy());
        position.setQuantity(transaction.getQuantity());
        position.setCost(cost);
        position.setRealizedPL(0.0);

        return position;
    }

    /**
     * processes a transaction on an existing position
     *
     * @return a pair containing the profit and profit percentage
     */
    public static TradePerformanceVO processTransaction(Position position, Transaction transaction) {

        // get old values
        long oldQty = position.getQuantity();
        double oldCost = position.getCost();
        double oldRealizedPL = position.getRealizedPL();
        double oldAvgPrice = position.getAveragePrice();

        // get transaction values
        long qty = transaction.getQuantity();
        double price = transaction.getPrice().doubleValue();
        double totalCharges = transaction.getTotalChargesDouble();
        int contractSize = transaction.getSecurity().getSecurityFamily().getContractSize();

        // calculate opening and closing quantity
        long closingQty = MathUtils.sign(oldQty) != MathUtils.sign(qty) ? Math.min(Math.abs(oldQty), Math.abs(qty)) * MathUtils.sign(qty) : 0;
        long openingQty = MathUtils.sign(oldQty) == MathUtils.sign(qty) ? qty : qty - closingQty;

        // calculate new values
        long newQty = oldQty + qty;
        double newCost = oldCost + openingQty * contractSize * price;
        double newRealizedPL = oldRealizedPL;

        // handle a previously non-closed position (aldAvgPrice is Double.NaN for a previously closed position)
        if (closingQty != 0) {
            newCost += closingQty * contractSize * oldAvgPrice;
            newRealizedPL += closingQty * contractSize * (oldAvgPrice - price);
        }

        // handle commissions
        if (openingQty != 0) {
            newCost += totalCharges * openingQty / qty;
        }

        if (closingQty != 0) {
            newRealizedPL -= totalCharges * closingQty / qty;
        }

        // calculate profit and profitPct
        TradePerformanceVO tradePerformance = null;
        if (Long.signum(position.getQuantity()) * Long.signum(transaction.getQuantity()) == -1) {

            double cost, value;
            if (Math.abs(transaction.getQuantity()) <= Math.abs(position.getQuantity())) {
                cost = position.getCost() * Math.abs((double) transaction.getQuantity() / (double) position.getQuantity());
                value = transaction.getNetValueDouble();
            } else {
                cost = position.getCost();
                value = transaction.getNetValueDouble() * Math.abs((double) position.getQuantity() / (double) transaction.getQuantity());
            }

            double profit = value - cost;
            double profitPct = Direction.LONG.equals(position.getDirection()) ? ((value - cost) / cost) : ((cost - value) / cost);

            tradePerformance = new TradePerformanceVO(profit, profitPct, profit > 0);
        }

        // set values
        position.setQuantity(newQty);
        position.setCost(newCost);
        position.setRealizedPL(newRealizedPL);

        return tradePerformance;
    }
}
