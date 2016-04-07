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
package ch.algotrader.service.sim;

import java.math.BigDecimal;

import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.service.SimpleOrderExecService;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface SimulationOrderService extends SimpleOrderExecService {

    /**
     * get the execution price for the specified Order. Per default limit orders are executed at
     * their limit price and all other orders are executed the the market. This method can be
     * overwritten to implement custom slippage models.
     */
    public BigDecimal getPrice(SimpleOrder order);

    /**
     * Get the execution commission for the specified Order. Per default values defined with the
     * corresponding SecurityFamily are used. Can be overwritten to implement custom commission
     * models.
     */
    public BigDecimal getExecutionCommission(SimpleOrder order);

    /**
     * Get the clearing commission for the specified Order. Per default values defined with the
     * corresponding SecurityFamily are used. Can be overwritten to implement custom commission
     * models.
     */
    public BigDecimal getClearingCommission(SimpleOrder order);

    /**
     * Get the fee for the specified Order. Per default values defined with the corresponding
     * SecurityFamily are used. Can be overwritten to implement custom commission models.
     */
    public BigDecimal getFee(SimpleOrder order);

}
