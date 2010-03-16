package com.algoTrader.util;

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

public class SMSAppender extends SMTPAppender {

    private int timeFrame;
    private int maxEMails;
    protected long timeFrameMillis;
    protected List<Date> exceptionDates = new ArrayList<Date>();

    public int getTimeFrame() {
        return timeFrame;
    }

    public void setTimeFrame(int timeFrame) {
        this.timeFrame = timeFrame;
    }

    public int getMaxEMails() {
        return maxEMails;
    }

    public void setMaxEMails(int maxEMails) {
        this.maxEMails = maxEMails;
    }

    @Override
    public void activateOptions() {

        super.activateOptions();
        timeFrameMillis = timeFrame * 60 * 1000;
        setBufferSize(1);
    }

    protected void cleanTimedoutExceptions() {

        Date current = new Date();

        // Remove timedout exceptions
        Iterator<Date> itr = exceptionDates.iterator();

        while (itr.hasNext()) {
            Date exceptionDate = itr.next();
            if (current.getTime() - exceptionDate.getTime() > timeFrameMillis) {
                itr.remove();
            } else {
                break;
            }
        }
    }

    protected void addException() {
        exceptionDates.add(new Date());
    }

    protected boolean isSendMailAllowed() {
        return exceptionDates.size() < maxEMails;
    }

    protected void sendBuffer() {

        cleanTimedoutExceptions();

        if (isSendMailAllowed()) {
            try {
                MimeBodyPart part = new MimeBodyPart();
                StringBuffer sbuf = new StringBuffer();
                String t = layout.getHeader();

                if (t != null) {
                    sbuf.append(t);
                }

                LoggingEvent event = cb.get();
                sbuf.append(layout.format(event));

                if (layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();
                    if (s != null && s.length > 0) {
                        sbuf.append(s[0]);
                    }
                    t = layout.getFooter();
                }

                if (t != null) {
                    sbuf.append(t);
                }

                part.setContent(sbuf.toString(), layout.getContentType());
                Multipart mp = new MimeMultipart();
                mp.addBodyPart(part);
                msg.setContent(mp);
                msg.setSentDate(new Date());

                Transport.send(msg);
                addException();

            } catch (Exception e) {
                LogLog.error("Error occured while sending e-mail notification.", e);
            }
        }
    }
}
