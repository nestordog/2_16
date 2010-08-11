package com.algoTrader.util.csv;

import com.algoTrader.entity.Order;
import com.espertech.esper.schedule.ScheduleSlot;
import com.espertech.esperio.AbstractSendableEvent;
import com.espertech.esperio.AbstractSender;

public class OrderSendableEvent extends AbstractSendableEvent {

    private final Order eventToSend;

    public OrderSendableEvent(Order order, long timestamp, ScheduleSlot scheduleSlot) {
        super(timestamp, scheduleSlot);

        this.eventToSend = order;
    }

    public void send(AbstractSender sender) {
        sender.sendEvent(this, this.eventToSend);
    }

    public String toString() {
        return this.eventToSend.toString();
    }

    public Object getBeanToSend() {
        return this.eventToSend;
    }
}
