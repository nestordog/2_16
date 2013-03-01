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

import java.util.HashSet;
import java.util.Set;

import javax.mail.Address;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;

import com.algoTrader.util.MyLogger;

/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EmailDispatcher {

    private static Logger logger = MyLogger.getLogger(EmailDispatcher.class.getName());

    private Set<Disposition> dispositions = new HashSet<Disposition>();

    public void setDispositions(Set<Disposition> dispositions) {
        this.dispositions = dispositions;
    }

    @Transformer
    public Message<javax.mail.Message> transformit(Message<javax.mail.Message> message) throws MessagingException {

        javax.mail.Message mm = message.getPayload();

        for (Disposition disposition : this.dispositions) {

            if (disposition.getFrom() != null && !containsAddress(mm.getFrom(), disposition.getFrom())) {
                continue;
            } else if (disposition.getTo() != null && !containsAddress(mm.getAllRecipients(), disposition.getTo())) {
                continue;
            } else if (disposition.getSubject() != null && !mm.getSubject().contains(disposition.getSubject())) {
                continue;
            }

            logger.info("processing message \"" + mm.getSubject() + "\" from " + mm.getFrom()[0] + " sent on " + mm.getSentDate() + " by disposition " + disposition.getName());

            // set the headers
            return MessageBuilder.fromMessage(message)
                    .setHeader("directory", disposition.getDirectory())
                    .setHeader("reconciliationService", disposition.getReconciliationService())
                    .build();
        }

        logger.info("ignoring message \"" + mm.getSubject() + "\" from " + mm.getFrom()[0] + " sent on " + mm.getSentDate());

        // return unmodified message
        return message;
    }

    private boolean containsAddress(Address[] addresses, String addressString) throws MessagingException {

        for (Address address : addresses) {
            if (address.toString().contains(addressString)) {
                return true;
            }
        }
        return false;
    }
}
