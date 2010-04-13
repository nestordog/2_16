package com.algoTrader.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.TickCsvWriter;

public abstract class TickServiceImpl extends TickServiceBase {

    private Map<Security, TickCsvWriter> csvWriters = new HashMap<Security, TickCsvWriter>();

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
                TickCsvWriter csvWriter;
                if (csvWriters.containsKey(security)) {
                    csvWriter = (TickCsvWriter)csvWriters.get(security);
                } else {
                    csvWriter = new TickCsvWriter(security.getIsin());
                    csvWriters.put(security, csvWriter);
                }
                csvWriter.write(tick);
            }
        }
    }
}
