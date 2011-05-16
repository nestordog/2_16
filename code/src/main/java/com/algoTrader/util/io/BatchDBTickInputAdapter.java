package com.algoTrader.util.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Tick;
import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.SendableEvent;

public class BatchDBTickInputAdapter extends AbstractCoordinatedAdapter {

    private static final int batchSize = 1;

    private Iterator<Tick> iterator = (new ArrayList<Tick>()).iterator();
    private Date startDate;

    public BatchDBTickInputAdapter(EPServiceProvider cep, Date startDate) {
        super(cep, true, true);
        this.startDate = startDate;
    }

    protected void close() {
        //do nothing
    }

    protected void replaceFirstEventToSend() {
        this.eventsToSend.remove(this.eventsToSend.first());
        SendableEvent event = read();
        if (event != null) {
            this.eventsToSend.add(event);
        }
    }

    protected void reset() {
        // do nothing
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SendableEvent read() throws EPException {
        if (this.stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if (this.eventsToSend.isEmpty()) {

            // get the next batch
            if (!this.iterator.hasNext()) {
                Date endDate = DateUtils.addDays(this.startDate, batchSize);

                List ticks = ServiceLocator.serverInstance().getLookupService().getTicksByTimePeriodOnWatchlist(this.startDate, endDate);
                // TODO: handle intraday put on watchlist by trades of that day

                if (ticks.size() > 0) {
                    this.iterator = ticks.iterator();
                    this.startDate = endDate;
                }
            }

            if (this.iterator.hasNext()) {

                Tick tick = this.iterator.next();
                return new SendableBaseObjectEvent(tick, tick.getDateTime().getTime(), this.scheduleSlot);

            } else {
                return null;
            }
        } else {
            SendableEvent event = this.eventsToSend.first();
            this.eventsToSend.remove(event);
            return event;
        }
    }
}
