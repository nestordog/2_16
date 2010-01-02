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
    private static OptionType optionType = OptionType.fromString(PropertiesUtil.getProperty("optionType"));

    public void update(Security underlaying, BigDecimal spot, BigDecimal volatility) {

        if (simulation) {

            StockOption option = ServiceLocator.instance().getStockOptionService().createStockOption(underlaying, new Date(), spot, optionType);
            option.setOnWatchlist(true);
            ServiceLocator.instance().getRuleService().activate("simulateStockOption");
        } else {

            StockOption option = ServiceLocator.instance().getStockOptionService().findStockOption(underlaying, new Date(), spot, optionType);
            option.setOnWatchlist(true);
            ServiceLocator.instance().getTickService().start(option);
        }

        ServiceLocator.instance().getRuleService().activate("marketTiming");
    }
}
