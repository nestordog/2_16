package com.espertech.esperio.csv;

import com.algoTrader.entity.Security;
import com.algoTrader.entity.Tick;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.SendableBeanEvent;
import com.espertech.esperio.SendableEvent;

public class TickCSVInputAdapter extends CSVInputAdapter {

    private Security security;

    public TickCSVInputAdapter(EPServiceProvider epService, CSVInputAdapterSpec spec, Security sec) {

        super(epService, spec);
        security = sec;
    }

    public SendableEvent read() throws EPException {
        SendableBeanEvent event = (SendableBeanEvent)super.read();

        if (event != null) {
            Tick tick = (Tick)event.getBeanToSend();
            tick.setSecurity(security);
        }

        return event;
    }
}
