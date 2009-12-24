package com.algoTrader.util;

import java.util.Date;

public class CustomDate extends Date {

    public CustomDate(String date) {
        super(Long.parseLong(date));
    }

    public CustomDate(long date) {
        super(date);
    }

}
