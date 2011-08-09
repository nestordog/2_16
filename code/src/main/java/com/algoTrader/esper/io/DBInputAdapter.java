package com.algoTrader.esper.io;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.beanutils.PropertyUtils;

import com.algoTrader.entity.BaseEntity;
import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.SendableEvent;

public class DBInputAdapter extends AbstractCoordinatedAdapter {

    private Iterator<? extends BaseEntity> iterator;
    private String timeStampColumn;

    public DBInputAdapter(EPServiceProvider cep, Collection<? extends BaseEntity> baseObjects, String timeStampColumn) {

        super(cep, true, true);

        this.iterator = baseObjects.iterator();
        this.timeStampColumn = timeStampColumn;
    }

    @Override
    protected void close() {
        //do nothing
    }

    @Override
    protected void replaceFirstEventToSend() {
        this.eventsToSend.remove(this.eventsToSend.first());
        SendableEvent event = read();
        if (event != null) {
            this.eventsToSend.add(event);
        }
    }

    @Override
    protected void reset() {
        // do nothing
    }

    @Override
    public SendableEvent read() throws EPException {
        if (this.stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if (this.eventsToSend.isEmpty()) {

            if (this.iterator.hasNext()) {

                try {
                    BaseEntity baseObject = this.iterator.next();
                    Date date = (Date) PropertyUtils.getProperty(baseObject, this.timeStampColumn);

                    return new SendableBaseObjectEvent(baseObject, date.getTime(), this.scheduleSlot);
                } catch (Exception e) {
                    throw new EPException("problem getting timestamp column", e);
                }

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
