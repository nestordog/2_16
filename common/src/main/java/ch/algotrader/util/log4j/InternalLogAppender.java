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
package ch.algotrader.util.log4j;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.message.Message;

import ch.algotrader.event.dispatch.EventDispatcher;
import ch.algotrader.event.dispatch.EventRecipient;
import ch.algotrader.vo.LogEventVO;

final class InternalLogAppender extends AbstractAppender {

    private static final long serialVersionUID = 1L;

    private final Level maxLevel;
    private final Level minLevel;
    private final EventDispatcher eventDispatcher;
    private final AtomicBoolean enabled;

    InternalLogAppender(final Level minLevel, final Level maxLevel, final EventDispatcher eventDispatcher) {
        super("Algotrader-internal", null, null);
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.eventDispatcher = eventDispatcher;
        this.enabled = new AtomicBoolean(false);
    }

    public void enable() {
        this.enabled.set(true);
    }

    public void disable() {
        this.enabled.set(false);
    }

    @Override
    public void append(final LogEvent event) {

        if (this.enabled.get()) {

            Level level = event.getLevel();
            if (level.isInRange(this.minLevel, this.maxLevel)) {
                Message message = event.getMessage();
                Throwable t = event.getThrown();
                this.eventDispatcher.broadcast(
                        new LogEventVO(level.toString(), message != null ? message.getFormattedMessage() : null, t),
                        EventRecipient.REMOTE_ONLY);
            }
        }
    }

}
