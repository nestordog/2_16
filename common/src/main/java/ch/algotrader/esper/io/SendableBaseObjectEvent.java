/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.esper.io;

import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

/**
 * Used by {@link DBTickInputAdapter}, {@link DBBarInputAdapter} and {@link CollectionInputAdapter}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
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
