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
package ch.algotrader.util.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.springframework.jms.support.converter.SimpleMessageConverter;

/**
 * A ActiveMQ {@link SimpleMessageConverter} that does not serialize {@link Collection Collections} or {@link Map Maps}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CollectionIgnoringMessageConverter extends SimpleMessageConverter {

    /**
     * Creates a {@link ObjectMessage} based on the given {@code serializable} by ignoring all  {@link Collection Collections} and {@link Map Maps}.
     */
    @Override
    protected ObjectMessage createMessageForSerializable(Serializable serializable, Session session) throws JMSException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new CollectionIgnoringObjectOutputStream(bos)) {
            oos.writeObject(serializable);

            ActiveMQObjectMessage message = (ActiveMQObjectMessage) session.createObjectMessage(); // empty ObjectMessage
            message.setContent(bos.toByteSequence());
            return message;

        } catch (IOException e) {
            throw new JMSException(e.toString());
        }
    }

    /**
     * custom ObjectOutputStream which will not serialize any Collections
     */
    private static class CollectionIgnoringObjectOutputStream extends ObjectOutputStream {

        public CollectionIgnoringObjectOutputStream(OutputStream out) throws IOException {
            super(out);
            enableReplaceObject(true);
        }

        @Override
        protected Object replaceObject(Object obj) throws IOException {

            if (obj instanceof Collection || obj instanceof Map) {

                // do not serialize Collections or Maps
                return null;
            } else {
                return super.replaceObject(obj);
            }
        }
    }
}
