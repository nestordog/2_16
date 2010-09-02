package com.algoTrader.service.ib;

import com.algoTrader.enumeration.Market;

public class IbMarketConverter {

    public static String marketToString(Market market) {

        if (Market.EUREX.equals(market))
            return "SOFFEX";

        if (Market.DE.equals(market))
            return "DTB";

        if (Market.US1.equals(market))
            return "SMART";

        if (Market.US2.equals(market))
            return "CBOE";

        throw new IllegalArgumentException("unknown market: " + market);
    }
}
