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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.commons.beanutils.PropertyUtils;

import com.espertech.esper.adapter.AdapterState;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esperio.AbstractCoordinatedAdapter;
import com.espertech.esperio.SendableEvent;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CollectionInputAdapter extends AbstractCoordinatedAdapter {

    private Iterator<?> iterator;
    private String timeStampColumn;

    public CollectionInputAdapter(EPServiceProvider cep, Collection<?> baseObjects, String timeStampColumn) {

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
                    Object baseObject = this.iterator.next();
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
