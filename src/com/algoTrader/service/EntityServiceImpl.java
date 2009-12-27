package com.algoTrader.service;

import org.apache.commons.lang.ArrayUtils;

import com.algoTrader.entity.Account;
import com.algoTrader.entity.Position;
import com.algoTrader.entity.Rule;
import com.algoTrader.entity.Security;
import com.algoTrader.entity.Transaction;

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

    @Override
    protected Security[] handleGetAllSecurities() throws Exception {


        return (Security[])getSecurityDao().loadAll().toArray(new Security[0]);
    }

    @Override
    protected Account handleGetAllAccounts() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Position handleGetAllPositions() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Rule handleGetAllRules() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Transaction handleGetAllTransactions() throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

}
