package com.algoTrader.service.ib;

import com.algoTrader.enumeration.Market;

public class IbMarketConverter {

    public static String marketToString(Market market) {

        return market.getValue();
    }
}
