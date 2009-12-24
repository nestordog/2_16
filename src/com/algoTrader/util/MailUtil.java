package com.algoTrader.util;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class MailUtil {

    private static String mailHost = PropertiesUtil.getProperty("mailHost");
    private static String mailPort = PropertiesUtil.getProperty("mailPort");
    private static String mailAddress = PropertiesUtil.getProperty("mailAddress");
    private static String mailPersonal = PropertiesUtil.getProperty("mailPersonal");
    private static String mailUsername = PropertiesUtil.getProperty("mailUsername");
    private static String mailPassword = PropertiesUtil.getProperty("mailPassword");

    public static void sendMail(String to, String subject, String body, boolean bcc) throws MessagingException, UnsupportedEncodingException {

        Message message = setUpMessage(to, subject, bcc);
        message.setContent(body, "text/plain");

        Transport.send(message);
    }

    public static void sendMail(String to, String subject, String body, File file, boolean bcc) throws MessagingException, UnsupportedEncodingException {

        Message message = setUpMessage(to, subject, bcc);

        BodyPart textBodyPart = new MimeBodyPart();
        textBodyPart.setText(body);

        BodyPart attachmentBodyPart = new MimeBodyPart();
        attachmentBodyPart.setDataHandler(new DataHandler(new FileDataSource(file)));
        attachmentBodyPart.setFileName(file.getName());

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(textBodyPart);
        multipart.addBodyPart(attachmentBodyPart);

        message.setContent(multipart);

        Transport.send(message);
    }

    private static Message setUpMessage(String to, String subject, boolean bcc) throws MessagingException, UnsupportedEncodingException {

        //Get system properties
        Properties props = System.getProperties();

        //Specify the desired SMTP server
        props.put("mail.smtp.host", mailHost);
        props.put("mail.smtp.port", mailPort);
        props.put("mail.smtp.auth", "true");

        // create a new Session object
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(mailUsername, mailPassword);
            }});

        // create a new MimeMessage object (using the Session created above)
        Message message = new MimeMessage(session);
        message.setSubject(subject);
        message.setFrom(new InternetAddress(mailAddress, mailPersonal));
        message.setRecipients(Message.RecipientType.TO, new InternetAddress[] { new InternetAddress(to) });

        if (bcc) {
            message.setRecipients(Message.RecipientType.BCC, new InternetAddress[] { new InternetAddress(mailAddress) });
        }

        return message;
    }
}
