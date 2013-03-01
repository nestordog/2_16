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
package com.algoTrader.util.io;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Collection;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQObjectMessage;
import org.apache.activemq.util.ByteArrayOutputStream;
import org.springframework.jms.support.converter.SimpleMessageConverter;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CollectionIgnoringMessageConverter extends SimpleMessageConverter {

    @Override
    protected ObjectMessage createMessageForSerializable(Serializable object, Session session) throws JMSException {

        ByteArrayOutputStream bos;
        try {
            bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new CollectionIgnoringObjectOutputStream(bos); // custom ObjectOutputStream
            oos.writeObject(object);
            oos.close();
        } catch (IOException e) {
            throw new JMSException(e.toString());
        }

        ActiveMQObjectMessage message = (ActiveMQObjectMessage) session.createObjectMessage(); // empty ObjectMessage
        message.setContent(bos.toByteSequence());
        return message;
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

            if (obj instanceof Collection) {
                // do not serialize any Collections
                return null;
            } else {
                return super.replaceObject(obj);
            }
        }
    }

}
