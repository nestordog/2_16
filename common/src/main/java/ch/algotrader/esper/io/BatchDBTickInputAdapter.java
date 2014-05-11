/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.esper.io;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.SendableEvent;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.marketData.Tick;

/**
 * A {@link com.espertech.esperio.CoordinatedAdapter} used to input {@link ch.algotrader.entity.marketData.Tick Ticks}
 * for all subscribed secruitiesin 1-day batches.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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
