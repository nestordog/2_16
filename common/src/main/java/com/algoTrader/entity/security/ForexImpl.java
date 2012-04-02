package com.algoTrader.entity.security;

import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.enumeration.Currency;

public class ForexImpl extends Forex {

    private static final long serialVersionUID = -6204294412084812111L;

    @Override
    public Currency getTransactionCurrency() {

        return getSecurityFamily().getCurrency();
    }

    @Override
    public boolean validateTick(Tick tick) {

        if (tick.getVolBid() == 0) {
            return false;
        } else if (tick.getVolAsk() == 0) {
            return false;
        } else if (tick.getBid().doubleValue() < 0) {
            return false;
        } else if (tick.getAsk().doubleValue() < 0) {
            return false;
        }

        return super.validateTick(tick);
    }
}
