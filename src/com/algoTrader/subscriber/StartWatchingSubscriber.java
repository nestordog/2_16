package com.algoTrader.subscriber;

import java.math.BigDecimal;
import java.util.Date;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.util.PropertiesUtil;

public class StartWatchingSubscriber {

    private static boolean simulation = new Boolean(PropertiesUtil.getProperty("simulation")).booleanValue();
    private static OptionType optionType = OptionType.fromString(PropertiesUtil.getProperty("simulation.optionType"));

    public void update(Security underlaying, BigDecimal spot, BigDecimal volatility) {

        StockOption option;
        if (simulation) {

            option = ServiceLocator.instance().getStockOptionService().createDummyStockOption(underlaying, new Date(), spot, optionType);
            option.setOnWatchlist(true);
            ServiceLocator.instance().getRuleService().activate("simulateDummySecurities");
        } else {

            option = ServiceLocator.instance().getStockOptionService().findNearestStockOption(underlaying, new Date(), spot, optionType);
            option.setOnWatchlist(true);
            ServiceLocator.instance().getTickService().start(option);
        }

        String[] parameters = { String.valueOf(option.getId()) };
        ServiceLocator.instance().getRuleService().activate("marketTiming", parameters);
        ServiceLocator.instance().getRuleService().activate("addToWatchlist");

    }
}
