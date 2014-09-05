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
package ch.algotrader.service;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface AccountService {

    /**
     * Returns the maximum number of Contracts that can be purchased based on the {@code
     * initialMarginPerContract} so that fractions are avoided in Sub-Accounts / Child-Accounts.
     */
    public long getQuantityByMargin(String strategyName, double initialMarginPerContractInBase);

    /**
     * Returns a number of Contracts that can be spread evenly amongst multiple Sub-Accounts /
     * Child-Accounts based on defined Allocations so that fractions are avoided.
     * <i>Note: the actual quantity might vary slightly from the {@code requestedQuantity}<i>
     */
    public long getQuantityByAllocation(String strategyName, long requestedQuantity);

}
