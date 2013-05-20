/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package com.algoTrader.esper.io;

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

/**
 * A {@link com.espertech.esperio.CoordinatedAdapter} used to input {@link com.algoTrader.entity.marketData.Tick Ticks}
 * for all subscribed secruitiesin 1-day batches.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class BatchDBTickInputAdapter extends AbstractCoordinatedAdapter {

    private static final int batchSize = 1;

    private Iterator<Tick> iterator = (new ArrayList<Tick>()).iterator();
    private Date startDate;

    public BatchDBTickInputAdapter(EPServiceProvider cep, Date startDate) {
        super(cep, true, true);
        this.startDate = startDate;
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public SendableEvent read() throws EPException {
        if (this.stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if (this.eventsToSend.isEmpty()) {

            // get the next batch
            if (!this.iterator.hasNext()) {
                Date endDate = DateUtils.addDays(this.startDate, batchSize);

                List ticks = ServiceLocator.instance().getLookupService().getSubscribedTicksByTimePeriod(this.startDate, endDate);
                // TODO: handle intraday subscription by trades of that day

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
