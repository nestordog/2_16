package com.algoTrader.util.mail;

import com.algoTrader.service.ReconciliationService;

public class Disposition {

    private String name;
    private String to;
    private String from;
    private String subject;
    private String directory;
    private ReconciliationService reconciliationService;

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

    public String getDirectory() {
        return this.directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public ReconciliationService getReconciliationService() {
        return this.reconciliationService;
    }

    public void setReconciliationService(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
