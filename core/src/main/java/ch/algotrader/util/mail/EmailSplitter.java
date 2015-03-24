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
package ch.algotrader.util.mail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;

/**
 * Splits a {@link Message} containing a {@link List} of {@link EmailFragment EmailFragments} into a
 * {@link List} of {@link Message Messages} with payload data.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EmailSplitter {

    @Splitter
    public List<Message<?>> splitIntoMessages(final Message<List<EmailFragment>> message) {

        final List<Message<?>> replyMessages = new ArrayList<>();

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
