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
package ch.algotrader.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.mail.Multipart;
import javax.mail.Transport;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A custom {@code org.apache.log4j.net.SMTPAppender} designed to compose SMS messages.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class SMSAppender extends SMTPAppender {

    private int timeFrame;
    private int maxEMails;
    protected long timeFrameMillis;
    protected List<Date> exceptionDates = new ArrayList<Date>();

    public int getTimeFrame() {
        return this.timeFrame;
    }

    public void setTimeFrame(int timeFrame) {
        this.timeFrame = timeFrame;
    }

    public int getMaxEMails() {
        return this.maxEMails;
    }

    public void setMaxEMails(int maxEMails) {
        this.maxEMails = maxEMails;
    }

    @Override
    public void activateOptions() {

        super.activateOptions();
        this.timeFrameMillis = this.timeFrame * 60 * 1000;
        setBufferSize(1);
    }

    protected void cleanTimedoutExceptions() {

        Date current = new Date();

        // Remove timedout exceptions
        Iterator<Date> itr = this.exceptionDates.iterator();

        while (itr.hasNext()) {
            Date exceptionDate = itr.next();
            if (current.getTime() - exceptionDate.getTime() > this.timeFrameMillis) {
                itr.remove();
            } else {
                break;
            }
        }
    }

    protected void addException() {
        this.exceptionDates.add(new Date());
    }

    protected boolean isSendMailAllowed() {
        return this.exceptionDates.size() < this.maxEMails;
    }

    @Override
    protected void sendBuffer() {

        cleanTimedoutExceptions();

        if (isSendMailAllowed()) {
            try {
                StringBuffer sbuf = new StringBuffer();

                LoggingEvent event = this.cb.get();

                String ex = null;
                if (this.layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();
                    if (s != null && s.length > 0) {
                        ex = s[0].substring(s[0].substring(0, s[0].lastIndexOf(":")).lastIndexOf(".") + 1).trim();
                    }
                }

                String m = this.layout.format(event).trim();

                if (ex != null & !"".equals(ex) && m != null && !"".equals(m)) {
                    sbuf.append(ex + " / " + m);
                } else if (ex != null & !"".equals(ex)) {
                    sbuf.append(ex);
                } else if (m != null & !"".equals(m)) {
                    sbuf.append(m);
                }

                String content = sbuf.toString();
                if (content.length() > 160) {
                    content = content.substring(0, 160);
                }

                MimeBodyPart part = new MimeBodyPart();
                part.setContent(content, this.layout.getContentType());
                Multipart mp = new MimeMultipart();
                mp.addBodyPart(part);
                this.msg.setContent(mp);
                this.msg.setSentDate(new Date());

                Transport.send(this.msg);
                addException();

            } catch (Exception e) {
                LogLog.error("Error occured while sending e-mail notification.", e);
            }
        }
    }
}
