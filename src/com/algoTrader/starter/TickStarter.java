package com.algoTrader.starter;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;

import com.algoTrader.ServiceLocator;

public class TickStarter {

    public static void main(String[] args) {

        List securities = new ArrayList();
        CollectionUtils.addAll(securities, args);
        ServiceLocator.instance().getTickService().run(securities);
    }
}
