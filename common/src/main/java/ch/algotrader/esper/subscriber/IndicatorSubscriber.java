/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.esper.subscriber;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import ch.algotrader.util.MyLogger;

/**
 * Prints all values as a comma-separated-list (CSV) to Log. (Headers are not available).
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class IndicatorSubscriber {

    private static Logger logger = MyLogger.getLogger(IndicatorSubscriber.class.getName());

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void update(Map<?, ?> map) {

        logger.info(StringUtils.join((new TreeMap(map)).values(), ","));
    }
}
