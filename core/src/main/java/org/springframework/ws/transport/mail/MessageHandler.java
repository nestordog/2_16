package org.springframework.ws.transport.mail;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.jms.IllegalStateException;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.scheduling.SchedulingAwareRunnable;

import com.algoTrader.service.ReconciliationService;

class MessageHandler implements SchedulingAwareRunnable {

    protected final Log logger = LogFactory.getLog(getClass());

    private final Message message;
    private final String name;
    private final ReconciliationService service;

    public MessageHandler(Message message, String name, ReconciliationService service) {

        this.name = name;
        this.message = message;
        this.service = service;
    }

    @Override
    public void run() {
        try {

            // handle parts/multiparts
            Object content = this.message.getContent();
            List<String> fileNames = new ArrayList<String>();
            if (content instanceof Multipart) {
                Multipart multipart = (Multipart) content;
                for (int i = 0, n = multipart.getCount(); i < n; i++) {
                    if (multipart.getBodyPart(i).getDisposition() != null) {
                        fileNames.add(handlePart(multipart.getBodyPart(i)));
                    }
                }
            } else {
                if (this.message.getDisposition() != null) {
                    fileNames.add(handlePart(this.message));
                }
            }

            // call the service
            this.service.reconcile(fileNames);
        }
        catch (Exception ex) {
            this.logger.error("Could not handle incoming mail connection", ex);
        }
    }

    public String handlePart(Part part) throws Exception {

        String disposition = part.getDisposition();
        if (disposition.equalsIgnoreCase(Part.ATTACHMENT) || disposition.equalsIgnoreCase(Part.INLINE)) {

            this.logger.debug("saving attachment: " + part.getFileName() + " : " + part.getContentType());

            File file = new File("results" + File.separator + this.name + File.separator + part.getFileName());
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);

            BufferedInputStream bis = new BufferedInputStream(part.getInputStream());
            int aByte;
            while ((aByte = bis.read()) != -1) {
                bos.write(aByte);
            }

            bos.flush();
            bos.close();
            bis.close();

            return file.toString();

        } else {
            throw new IllegalStateException("unkown disposition " + disposition);
        }
    }

    @Override
    public boolean isLongLived() {
        return false;
    }
}
