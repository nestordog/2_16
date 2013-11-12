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
package ch.algotrader.util.mail;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeBodyPart;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;

import ch.algotrader.util.MyLogger;

/**
 * Parses the E-mail Message and converts each containing message and/or attachment into
 * a {@link List} of {@link EmailFragment EmailFragments}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EmailTransformer {

    private static final Logger logger = MyLogger.getLogger(EmailTransformer.class.getName());

    @Transformer
    public Message<List<EmailFragment>> transform(Message<javax.mail.Message> message) throws MessagingException {

        javax.mail.Message mailMessage = message.getPayload();

        final List<EmailFragment> emailFragments = new ArrayList<EmailFragment>();

        handleMessage(mailMessage, emailFragments);

        Message<List<EmailFragment>> replyMessage = MessageBuilder.withPayload(emailFragments).copyHeaders(message.getHeaders()).build();

        return replyMessage;
    }

    /**
     * Parses a mail message.
     *
     * If the mail message is an instance of {@link Multipart} then we delegate
     * to {@link #handleMultipart(Multipart, List)}.
     * @throws MessagingException
     */
    public void handleMessage(final javax.mail.Message mailMessage, final List<EmailFragment> emailFragments) throws MessagingException {

        final Object content;

        try {
            content = mailMessage.getContent();
        } catch (IOException e) {
            throw new MessagingException("error while retrieving the email contents.", e);
        }

        // only handle multi part messages
        if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            handleMultipart(multipart, emailFragments);
        } else {
            throw new MessagingException("message is not a multipart message");
        }
    }

    /**
     * Parses any {@link Multipart} instances that contains attachments
     *
     * Will create the respective {@link EmailFragment}s representing those attachments.
     * @throws MessagingException
     */
    public void handleMultipart(Multipart multipart, List<EmailFragment> emailFragments) throws MessagingException {

        final int count = multipart.getCount();

        for (int i = 0; i < count; i++) {

            BodyPart bodyPart = multipart.getBodyPart(i);
            String filename = bodyPart.getFileName();
            String disposition = bodyPart.getDisposition();

            if (filename == null && bodyPart instanceof MimeBodyPart) {
                filename = ((MimeBodyPart) bodyPart).getContentID();
            }

            if (disposition == null) {

                //ignore message body

            } else if (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE)) {

                try {
                    BufferedInputStream bis = new BufferedInputStream(bodyPart.getInputStream());
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    try {

                        IOUtils.copy(bis, bos);

                        emailFragments.add(new EmailFragment(filename, bos.toByteArray()));

                        logger.info(String.format("processing file: %s", new Object[] { filename }));

                    } finally {
                        bos.close();
                        bis.close();
                    }

                } catch (IOException e) {
                    throw new MessagingException("error processing streams", e);
                }

            } else {
                throw new MessagingException("unkown disposition " + disposition);
            }
        }
    }
}
