package com.algoTrader.entity.security;

import com.algoTrader.entity.marketData.Tick;

public class NaturalIndexImpl extends NaturalIndex {

    private static final long serialVersionUID = 4087343403938105040L;

    @Override
    public boolean validateTick(Tick tick) {

        if (tick.getLast() == null) {
            return false;
        } else if (tick.getLastDateTime() == null) {
            return false;
        }

        return super.validateTick(tick);
    }
}
