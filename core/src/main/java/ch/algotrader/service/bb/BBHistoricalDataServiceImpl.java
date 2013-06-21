package ch.algotrader.service.bb;

import java.util.Date;
import java.util.List;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.enumeration.BarType;
import ch.algotrader.enumeration.TimePeriod;

public class BBHistoricalDataServiceImpl extends BBHistoricalDataServiceBase {

    private static final long serialVersionUID = 1339545758324165650L;

    @Override
    protected void handleInit() throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    protected List<Bar> handleGetHistoricalBars(int securityId, Date endDate, int timePeriodLength, TimePeriod timePeriod, int barSize,
            TimePeriod barSizePeriod, BarType barType) throws Exception {
        // TODO Auto-generated method stub
        return null;
    }
}
