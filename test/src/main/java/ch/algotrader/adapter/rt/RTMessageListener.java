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
package ch.algotrader.adapter.rt;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * JMS MessageListener for RealTickMarketOrderTest
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
class RTMessageListener implements MessageListener {

    private final LinkedBlockingQueue<Serializable> queue;

    public RTMessageListener(final LinkedBlockingQueue<Serializable> queue) {
        super();
        this.queue = queue;
    }

    @Override
    public void onMessage(final Message message) {
        try {
            try {
                if (message instanceof ObjectMessage) {
                    ObjectMessage objmsg = (ObjectMessage) message;
                    this.queue.put(objmsg.getObject());
                } else {
                    org.junit.Assert.fail("Unxpected message: " + message);
                }
            } catch (InterruptedException e) {
            } finally {
                message.acknowledge();
            }
        } catch (JMSException ex) {
            org.junit.Assert.fail(ex.getMessage());
        }
    }
}
