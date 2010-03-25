package com.algoTrader.entity;

import java.math.BigDecimal;

import com.algoTrader.enumeration.TransactionType;
import com.algoTrader.util.RoundUtil;

public class StockOptionImpl extends com.algoTrader.entity.StockOption {

    private static final long serialVersionUID = -3168298592370987085L;

    public BigDecimal getCommission(long quantity, TransactionType transactionType) {

        if (TransactionType.SELL.equals(transactionType) || TransactionType.BUY.equals(transactionType)) {
            if (quantity < 4) {
                return RoundUtil.getBigDecimal(quantity * 1.5 + 5);
            } else {
                return RoundUtil.getBigDecimal(quantity * 3);
            }
        } else {
            return new BigDecimal(0);
        }
    }

    public BigDecimal getCurrentValuePerContract() {

        double currentValuePerContract = getContractSize() * getLastTick().getCurrentValue().doubleValue();
        return RoundUtil.getBigDecimal(currentValuePerContract);
    }
}
