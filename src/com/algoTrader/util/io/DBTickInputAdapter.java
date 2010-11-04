package com.algoTrader.util.io;

import java.util.Collection;
import java.util.Iterator;

import com.algoTrader.entity.Tick;
import com.algoTrader.util.EsperService;
import com.espertech.esper.client.EPException;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.AdapterState;
import com.espertech.esperio.SendableEvent;


public class DBTickInputAdapter extends AbstractCoordinatedAdapter {

    private Iterator<Tick> ticktIterator;

    public DBTickInputAdapter(Collection<Tick> ticks) {

        super(EsperService.getEPServiceInstance(), true, true);

        this.ticktIterator = ticks.iterator();
    }

    protected void close() {
        //do nothing
    }

    protected void replaceFirstEventToSend() {
        this.eventsToSend.remove(this.eventsToSend.first());
        SendableEvent event = read();
        if(event != null) {
            this.eventsToSend.add(event);
        }
    }

    protected void reset() {
        // do nothing
    }

    public SendableEvent read() throws EPException {
        if(this.stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if(this.eventsToSend.isEmpty()) {

            if (this.ticktIterator.hasNext()) {

                Tick tick = this.ticktIterator.next();

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
