package com.algoTrader.esper.io;

import com.algoTrader.entity.BaseEntity;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

public class SendableBaseObjectEvent extends AbstractSendableEvent {

    private final BaseEntity eventToSend;

    public SendableBaseObjectEvent(BaseEntity object, long timestamp, ScheduleSlot scheduleSlot) {
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
