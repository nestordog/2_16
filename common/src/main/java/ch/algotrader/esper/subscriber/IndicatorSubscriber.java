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
package ch.algotrader.esper.subscriber;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Prints all values as a comma-separated-list (CSV) to Log. (Headers are not available).
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class IndicatorSubscriber {

    private static final Logger LOGGER = LogManager.getLogger(IndicatorSubscriber.class);

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(Map<?, ?> map) {

        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(StringUtils.join((new TreeMap(map)).values(), ","));
        }
    }
}
