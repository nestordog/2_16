package org.springframework.ws.transport.mail;

import com.algoTrader.service.ReconciliationService;

public class Disposition {

    private String name;
    private String to;
    private String from;
    private String subject;
    private ReconciliationService service;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getSubject() {
        return this.subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public ReconciliationService getService() {
        return this.service;
    }

    public void setService(ReconciliationService service) {
        this.service = service;
    }

}
