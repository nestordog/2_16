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
package com.algoTrader.util.mail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EmailSplitter {

    @Splitter
    public List<Message<?>> splitIntoMessages(final Message<List<EmailFragment>> message) {

        final List<Message<?>> replyMessages = new ArrayList<Message<?>>();

        for (EmailFragment fragement : message.getPayload()) {

            Message<?> replyMessage = MessageBuilder.withPayload(fragement.getData())
                    .copyHeaders(message.getHeaders())
                    .setHeader(FileHeaders.FILENAME, fragement.getFilename())
                    .build();

            replyMessages.add(replyMessage);
        }

        return replyMessages;
    }
}
