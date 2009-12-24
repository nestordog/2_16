package com.algoTrader.service;

public class EntityServiceImpl extends com.algoTrader.service.EntityServiceBase {

    protected com.algoTrader.entity.Security handleGetSecurity(int id) throws java.lang.Exception {

        return getSecurityDao().load(id);
    }

    protected com.algoTrader.entity.Account handleGetAccount(int id) throws java.lang.Exception {
        // @todo implement protected com.algoTrader.entity.Account
        // handleGetAccount(int id)
        return null;
    }

    protected com.algoTrader.entity.Position handleGetPosition(int id) throws java.lang.Exception {
        // @todo implement protected com.algoTrader.entity.Position
        // handleGetPosition(int id)
        return null;
    }

    protected com.algoTrader.entity.Rule handleGetRule(int id) throws java.lang.Exception {
        // @todo implement protected com.algoTrader.entity.Rule
        // handleGetRule(int id)
        return null;
    }

    protected com.algoTrader.entity.Transaction handleGetTransaction(int id) throws java.lang.Exception {
        // @todo implement protected com.algoTrader.entity.Transaction
        // handleGetTransaction(int id)
        return null;
    }

}
