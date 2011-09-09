package com.algoTrader.service.sq;

import javax.naming.OperationNotSupportedException;

public class SQHistoricalDataServiceImpl extends SQHistoricalDataServiceBase {

    @Override
    protected void handleRequestHistoricalData(int[] securityIds, String[] whatToShow, String startDate, String endDate) throws Exception {

        throw new OperationNotSupportedException("handleRequestHistoricalData not implemented yet");
    }

    @Override
    protected void handleRequestHistoricalData(int[] securityIds, String[] whatToShow, String[] startDate, String[] endDate) throws Exception {

        throw new OperationNotSupportedException("handleRequestHistoricalData not implemented yet");
    }
}
