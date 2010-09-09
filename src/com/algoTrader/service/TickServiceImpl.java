package com.algoTrader.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.StockOption;
import com.algoTrader.entity.Tick;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.csv.CsvTickWriter;

public abstract class TickServiceImpl extends TickServiceBase {

    private static Logger logger = MyLogger.getLogger(TickServiceImpl.class.getName());

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();

    @SuppressWarnings("unchecked")
    protected void handleProcessSecuritiesOnWatchlist() throws SuperCSVException, IOException  {

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            Tick tick = retrieveTick(security);

            if (tick != null) {

                try {
                    tick.validate();
                    EsperService.sendEvent(tick);
                } catch (Exception e) {
                    // do nothing, just ignore invalideTicks
                }

                // write the tick to file (even if not valid)
                CsvTickWriter csvWriter;
                if (this.csvWriters.containsKey(security)) {
                    csvWriter = this.csvWriters.get(security);
                } else {
                    csvWriter = new CsvTickWriter(security.getIsin());
                    this.csvWriters.put(security, csvWriter);
                }
                csvWriter.write(tick);
            }
        }
    }

    protected void handlePutOnWatchlist(int stockOptionId) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        putOnWatchlist(stockOption);
    }

    @SuppressWarnings("unchecked")
    protected void handlePutOnWatchlist(StockOption stockOption) throws Exception {

        if (!stockOption.isOnWatchlist()) {

            putOnExternalWatchlist(stockOption);

            stockOption.setOnWatchlist(true);
            getStockOptionDao().update(stockOption);
            getStockOptionDao().getStockOptionsOnWatchlist(false).add(stockOption);

            logger.info("put stockOption on watchlist " + stockOption.getSymbol());
        }
    }

    protected void handleRemoveFromWatchlist(int stockOptionId) throws Exception {

        StockOption stockOption = (StockOption)getStockOptionDao().load(stockOptionId);
        removeFromWatchlist(stockOption);
    }

    protected void handleRemoveFromWatchlist(StockOption stockOption) throws Exception {

        if (stockOption.isOnWatchlist()) {

            removeFromExternalWatchlist(stockOption);

            stockOption.setOnWatchlist(false);
            getStockOptionDao().update(stockOption);
            getStockOptionDao().getStockOptionsOnWatchlist(false).remove(stockOption);

            logger.info("removed stockOption from watchlist " + stockOption.getSymbol());
        }
    }
}
