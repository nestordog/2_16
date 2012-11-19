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
