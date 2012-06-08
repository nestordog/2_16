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

                String header = this.layout.getHeader();
                if (header != null) {
                    sbuf.append(header);
                }

                LoggingEvent event = this.cb.get();

                String ex = null;
                if (this.layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();
                    if (s != null && s.length > 0) {
                        ex = s[0].substring(s[0].substring(0, s[0].lastIndexOf(":")).lastIndexOf(".") + 1).trim();
                    }
                }

                String m = this.layout.format(event).trim();
                sbuf.append(ex != null & !"".equals(ex) && m != null && !"".equals(m) ? ex + " / " + m : ex + m);

                String footer = this.layout.getFooter();
                if (footer != null) {
                    sbuf.append(footer);
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
