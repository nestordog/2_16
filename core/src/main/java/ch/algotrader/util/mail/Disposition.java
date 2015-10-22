/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.util.mail;

import ch.algotrader.service.ReconciliationService;

/**
 * Pojo that defines Mail-Dispatching Rules.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
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
