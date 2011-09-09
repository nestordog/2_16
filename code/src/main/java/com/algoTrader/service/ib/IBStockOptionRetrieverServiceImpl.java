package com.algoTrader.service.ib;

import java.math.BigDecimal;
import java.util.Date;

import com.algoTrader.entity.security.StockOption;
import com.algoTrader.enumeration.ConnectionState;
import com.algoTrader.enumeration.OptionType;

public class IbStockOptionRetrieverServiceImpl extends IbStockOptionRetrieverServiceBase {

    private static final long serialVersionUID = 6446509772400405052L;

    @Override
    protected void handleRetrieveAllStockOptionsForUnderlaying(int underlayingId) throws Exception {
        throw new UnsupportedOperationException("handleRetrieveAllStockOptions is not implemented yet");
    }

    @Override
    protected void handleRetrieveAllStockOptions() throws Exception {
        throw new UnsupportedOperationException("handleRetrieveAllStockOptions is not implemented yet");
    }

    @Override
    protected StockOption handleRetrieveStockOption(int underlayingId, Date expiration, BigDecimal strike, OptionType type) throws Exception {
        throw new UnsupportedOperationException("handleRetrieveStockOption is not implemented yet");
    }

    @Override
    protected void handleInit() throws Exception {
        throw new UnsupportedOperationException("handleInit is not implemented yet");
    }

    @Override
    protected void handleConnect() throws Exception {
        throw new UnsupportedOperationException("handleConnect is not implemented yet");
    }

    @Override
    protected ConnectionState handleGetConnectionState() throws Exception {
        throw new UnsupportedOperationException("handleGetConnectionState is not implemented yet");
    }
}
