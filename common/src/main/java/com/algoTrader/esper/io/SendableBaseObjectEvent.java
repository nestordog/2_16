package com.algoTrader.esper.io;

import com.algoTrader.entity.PrintableI;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

public class SendableBaseObjectEvent extends AbstractSendableEvent {

    private final PrintableI eventToSend;

    public SendableBaseObjectEvent(PrintableI object, long timestamp, ScheduleSlot scheduleSlot) {
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
