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
package ch.algotrader.vo;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import ch.algotrader.entity.PositionVO;
import ch.algotrader.entity.strategy.CashBalanceVO;

/**
 * A ValueObject representing a result of a transaction.
 */
public class TransactionResultVO implements Serializable {

    private static final long serialVersionUID = -1166804141530510654L;

    private final PositionVO positionMutation;
    private final TradePerformanceVO tradePerformance;
    private final List<CashBalanceVO> cashBalances;


    public TransactionResultVO(final PositionVO positionMutation, final TradePerformanceVO tradePerformance, final List<CashBalanceVO> cashBalances) {
        this.positionMutation = positionMutation;
        this.tradePerformance = tradePerformance;
        this.cashBalances = cashBalances != null ? Collections.unmodifiableList(cashBalances) : Collections.emptyList();
    }

    public PositionVO getPositionMutation() {
        return positionMutation;
    }

    public TradePerformanceVO getTradePerformance() {
        return tradePerformance;
    }

    public List<CashBalanceVO> getCashBalances() {
        return cashBalances;
    }

    @Override
    public String toString() {
        return "{" +
                "positionMutation=" + positionMutation +
                ", tradePerformance=" + tradePerformance +
                ", cashBalances=" + cashBalances +
                '}';
    }
}
