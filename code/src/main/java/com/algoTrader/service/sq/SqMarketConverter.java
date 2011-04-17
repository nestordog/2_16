package com.algoTrader.service.sq;

import com.algoTrader.enumeration.Market;

public class SqMarketConverter {

    public static String marketToString(Market market) {

        return "21";

        // if (Market.SWX.equals(market))
        // return "M9";
        // if (Market.CH2.equals(market))
        // return "M3";
        // if (Market.EUREX.equals(market))
        // return "21";
        // if (Market.ISE.equals(market))
        // return "733";
        // if (Market.US1.equals(market))
        // return "65";
        // if (Market.US2.equals(market))
        // return "66";
        // if (Market.US3.equals(market))
        // return "67";
        // if (Market.US4.equals(market))
        // return "69";
        // if (Market.EU.equals(market))
        // return "13";
        // if (Market.DE.equals(market))
        // return "44";
        // if (Market.FI.equals(market))
        // return "40";
        // if (Market.BE.equals(market))
        // return "6";
        // if (Market.FR.equals(market))
        // return "25";
        // if (Market.IT.equals(market))
        // return "14";
        // throw new IllegalArgumentException("unrecognized market: " + market);
    }

    public static Market marketFromString(String market) {

        return Market.SOFFEX;

        // if ("M9".equals(market))
        // return Market.SWX;
        // if ("M3".equals(market))
        // return Market.CH2;
        // if ("21".equals(market))
        // return Market.SOFFEX;
        // if ("733".equals(market))
        // return Market.ISE;
        // if ("65".equals(market))
        // return Market.US1;
        // if ("66".equals(market))
        // return Market.US2;
        // if ("67".equals(market))
        // return Market.US3;
        // if ("69".equals(market))
        // return Market.US4;
        // if ("13".equals(market))
        // return Market.EU;
        // if ("44".equals(market))
        // return Market.DTB;
        // if ("40".equals(market))
        // return Market.FI;
        // if ("6".equals(market))
        // return Market.BE;
        // if ("25".equals(market))
        // return Market.FR;
        // if ("14".equals(market))
        // return Market.IT;
        //
        // throw new IllegalArgumentException("unrecognized market: " + market);
    }
}
