package com.algoTrader.starter;

import com.algoTrader.ServiceLocator;

public class NoActionStarter {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.LOCAL_BEAN_REFERENCE_LOCATION);
        ServiceLocator.instance().getContext();
    }
}
