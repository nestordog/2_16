package com.algoTrader.util.mail;

import java.util.ArrayList;
import java.util.List;

import org.springframework.integration.Message;
import org.springframework.integration.annotation.Splitter;
import org.springframework.integration.file.FileHeaders;
import org.springframework.integration.support.MessageBuilder;

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
