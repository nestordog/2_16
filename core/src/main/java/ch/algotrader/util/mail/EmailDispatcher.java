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

import java.util.HashSet;
import java.util.Set;

import javax.mail.Address;
import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.integration.Message;
import org.springframework.integration.annotation.Transformer;
import org.springframework.integration.support.MessageBuilder;

import ch.algotrader.util.MyLogger;

/**
 * Dispatches a {link Message} based on the defined {@link Disposition Dispositions}.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class EmailDispatcher {

    private static Logger logger = MyLogger.getLogger(EmailDispatcher.class.getName());

    private Set<Disposition> dispositions = new HashSet<Disposition>();

    public void setDispositions(Set<Disposition> dispositions) {
        this.dispositions = dispositions;
    }

    /**
     * Checks a {@link Message} against defined {@link Disposition Dispositions}. If a matching {@link Disposition} is found,
     * the {@code reconciliationService} and {@code directory} headers are set on the message.
     */
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
