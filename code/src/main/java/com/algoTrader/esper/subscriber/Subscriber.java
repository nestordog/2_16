package com.algoTrader.esper.subscriber;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;

public class Subscriber {

    protected static Logger logger = MyLogger.getLogger(Subscriber.class.getName());

    private Object service;

    public void setService(Object service) {
        this.service = service;
    }

    public Object getService() {
        return this.service;
    }
}
