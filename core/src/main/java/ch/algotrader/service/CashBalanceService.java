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

import ch.algotrader.entity.Transaction;
import ch.algotrader.vo.CurrencyAmountVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public interface CashBalanceService {

    /**
     * Adjusts CashBalances according to the specified Transaction.
     */
    public void processTransaction(Transaction transaction);

    /**
     * Adjusts CashBalances according to the specified Amount.
     */
    public void processAmount(String strategyName, CurrencyAmountVO amount);

    /**
     * Calculates all Cash Balances based on Transactions in the database and makes adjustments if
     * necessary
     */
    public String resetCashBalances();

}
