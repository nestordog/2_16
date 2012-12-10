package com.algoTrader.esper;

import com.algoTrader.enumeration.Duration;

/**
 * this class exists because esper cannot invoke methods (e.g. value()) on enumartions
 */
public class Constants {

    public static final long ONE_MINUTE = Duration.ONE_MINUTE.value();

    public static final long ONE_HOUR = Duration.ONE_HOUR.value();

    public static final long ONE_DAY = Duration.ONE_DAY.value();

    public static final long ONE_WEEK = Duration.ONE_WEEK.value();

    public static final long ONE_YEAR = Duration.ONE_YEAR.value();
}
