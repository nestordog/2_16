package com.algoTrader.service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.supercsv.exception.SuperCSVException;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.csv.CsvTickWriter;

public abstract class TickServiceImpl extends TickServiceBase {

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
}
