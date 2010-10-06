// license-header java merge-point
/**
 * This is only generated once! It will never be overwritten.
 * You can (and have to!) safely modify it by hand.
 */
package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.util.Date;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.enumeration.OptionType;
import com.algoTrader.enumeration.TransactionType;

/**
 * @see com.algoTrader.service.ib.IbStockOptionRetrieverService
 */
public class IbStockOptionRetrieverServiceImpl
    extends com.algoTrader.service.ib.IbStockOptionRetrieverServiceBase
{

    @Override
    protected void handleRetrieveAllStockOptions(Security underlaying) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected void handleRetrieveAllStockOptions() throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    protected StockOption handleRetrieveStockOption(Security underlaying, Date expiration, BigDecimal strike, OptionType type) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean handleVerifyVolatility(StockOption stockOption, TransactionType transactionType) throws Exception {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void handleInit() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    protected void handleConnect() throws Exception {
        // TODO Auto-generated method stub
    }
}
