package com.algoTrader.service;

import java.text.DecimalFormat;
import java.util.List;

import org.apache.log4j.Logger;

import com.algoTrader.entity.WatchListItem;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;

public class WatchListServiceImpl extends WatchListServiceBase {

    private static Logger logger = MyLogger.getLogger(WatchListServiceImpl.class.getName());
    private static DecimalFormat decimalFormat = new DecimalFormat("#,##0.0000");

    @Override
    protected void handleSetAlertValue(String strategyName, int securityId, Double value, boolean upper) throws Exception {

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);

        if (upper) {
            watchListItem.setUpperAlertValue(value);
            logger.info("set upper alert value to " + decimalFormat.format(value) + " for watchListItem " + watchListItem);
        } else {
            watchListItem.setLowerAlertValue(value);
            logger.info("set lower alert value to " + decimalFormat.format(value) + " for watchListItem " + watchListItem);
        }

        getWatchListItemDao().update(watchListItem);

    }

    @Override
    protected void handleRemoveAlertValues(String strategyName, int securityId) throws Exception {

        WatchListItem watchListItem = getWatchListItemDao().findByStrategyAndSecurity(strategyName, securityId);
        if (watchListItem.getUpperAlertValue() != null || watchListItem.getLowerAlertValue() != null) {

            watchListItem.setUpperAlertValue(null);
            watchListItem.setLowerAlertValue(null);

            getWatchListItemDao().update(watchListItem);

            logger.info("removed alert values for watchListItem " + watchListItem);
        }
    }

    @Override
    protected void handleRemoveNonPositionWatchListItem(String strategyName) throws Exception {

        List<WatchListItem> watchListItems = getWatchListItemDao().findNonPositionWatchListItem(strategyName);
        getWatchListItemDao().remove(watchListItems);
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleRemoveNonPositionWatchListItemByType(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        List<WatchListItem> watchListItems = getWatchListItemDao().findNonPositionWatchListItemByType(strategyName, discriminator);
        getWatchListItemDao().remove(watchListItems);

        if (watchListItems.size() > 0) {
            logger.debug("deleted non-position watchListItems: " + watchListItems);
        }
    }
}
