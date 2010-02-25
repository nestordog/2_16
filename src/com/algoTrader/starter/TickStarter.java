package com.algoTrader.starter;

import java.io.IOException;

import com.algoTrader.ServiceLocator;
import com.algoTrader.util.HttpClientUtil;

public class TickStarter {

    public static void main(String[] args) throws IOException {

        HttpClientUtil.initTradePassword();
        ServiceLocator.instance().getRuleService().activateAll();
    }
}
