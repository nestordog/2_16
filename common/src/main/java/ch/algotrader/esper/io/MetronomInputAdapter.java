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

import java.util.Date;
import java.util.TimeZone;

import org.springframework.scheduling.support.CronSequenceGenerator;

import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.client.EPException;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.SendableEvent;

/**
 * A {@link com.espertech.esperio.CoordinatedAdapter} that sends Metronom events based on the give cronPattern
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class MetronomInputAdapter extends AbstractCoordinatedAdapter {

    private final Date endDate;
    private final CronSequenceGenerator cron;

    private Date date;

    public MetronomInputAdapter(String cronPattern, Date startDate, Date endDate) {

        super(null, true, true);
        this.endDate = endDate;

        cron = new CronSequenceGenerator(cronPattern, TimeZone.getDefault());
        date = cron.next(startDate);
    }

    @Override
    protected void close() {
        //do nothing
    }

    @Override
    protected void replaceFirstEventToSend() {

        eventsToSend.remove(eventsToSend.first());
        SendableEvent event = read();
        if (event != null) {
            eventsToSend.add(event);
        }
    }

    @Override
    protected void reset() {
        // do nothing
    }

    @Override
    public SendableEvent read() throws EPException {

        if (stateManager.getState() == AdapterState.DESTROYED) {
            return null;
        }

        if (eventsToSend.isEmpty()) {

            while ((date = cron.next(date)).compareTo(endDate) <= 0) {
                return new SendableBaseObjectEvent(new Object(), date.getTime(), scheduleSlot);
            }

            return null;

        } else {

            SendableEvent event = eventsToSend.first();
            eventsToSend.remove(event);
            return event;
        }
    }
}
