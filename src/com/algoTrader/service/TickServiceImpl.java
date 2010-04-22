package com.algoTrader.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.csv.CsvTickWriter;

public abstract class TickServiceImpl extends TickServiceBase {

    private Map<Security, CsvTickWriter> csvWriters = new HashMap<Security, CsvTickWriter>();

    @SuppressWarnings("unchecked")
    protected void handleProcessSecuritiesOnWatchlist() throws Exception {

        List<Security> securities = getSecurityDao().findSecuritiesOnWatchlist();
        for (Security security : securities) {

            Tick tick = retrieveTick(security);

            if (tick != null) {

                if (tick.isValid()) {
                    EsperService.sendEvent(tick);
                }

                // write the tick to file (even if not valid)
                CsvTickWriter csvWriter;
                if (csvWriters.containsKey(security)) {
                    csvWriter = (CsvTickWriter)csvWriters.get(security);
                } else {
                    csvWriter = new CsvTickWriter(security.getIsin());
                    csvWriters.put(security, csvWriter);
                }
                csvWriter.write(tick);
            }
        }
    }
}
