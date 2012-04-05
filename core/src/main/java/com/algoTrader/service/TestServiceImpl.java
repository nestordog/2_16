package com.algoTrader.service;

import java.util.Date;

import com.algoTrader.entity.security.Future;


public class TestServiceImpl extends TestServiceBase {

    @Override
    protected void handleTest() throws Exception {

        Future future = getFutureDao().findByMinExpiration(22, new Date(0));
        System.out.println(future);

        getCombinationService().addComponent(20, 8, 1);

        getComponentDao().findSubscribedBySecurity(20);
        getComponentDao().findSubscribedBySecurity(21);
        getComponentDao().findSubscribedBySecurity(22);
        getComponentDao().findSubscribedBySecurity(23);

        getCombinationService().addComponent(20, 8, 1);
        getComponentDao().findSubscribedBySecurity(20);
        getComponentDao().findSubscribedBySecurity(21);
        getComponentDao().findSubscribedBySecurity(22);
        getComponentDao().findSubscribedBySecurity(23);
    }
}
