package com.algoTrader.util;

import java.util.Date;

public class DateUtil {

    public static Date toDate(long time) {

        return new Date(time);
    }

    public static Date getCurrentEPTime() {

        return new Date(EsperService.getEPServiceInstance().getEPRuntime().getCurrentTime());
    }
}
