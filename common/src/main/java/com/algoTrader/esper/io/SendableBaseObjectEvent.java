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

import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SendableBaseObjectEvent extends AbstractSendableEvent {

    private final Object eventToSend;

    public SendableBaseObjectEvent(Object object, long timestamp, ScheduleSlot scheduleSlot) {
        super(timestamp, scheduleSlot);

        this.eventToSend = object;
    }

    @Override
    public void send(AbstractSender sender) {
        sender.sendEvent(this, this.eventToSend);
    }

    @Override
    public String toString() {
        return this.eventToSend.toString();
    }

    public Object getBeanToSend() {
        return this.eventToSend;
    }
}
