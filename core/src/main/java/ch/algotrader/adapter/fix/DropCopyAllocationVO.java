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
package ch.algotrader.adapter.fix;

import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public final class DropCopyAllocationVO {

    private final Security security;
    private final Account account;
    private final Strategy strategy;

    public DropCopyAllocationVO(final Security security, final Account account, final Strategy strategy) {
        this.security = security;
        this.account = account;
        this.strategy = strategy;
    }

    public Security getSecurity() {
        return security;
    }

    public Account getAccount() {
        return account;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    @Override
    public String toString() {
        return "{" +
                "security=" + security +
                ", account=" + account +
                ", strategy=" + strategy +
                '}';
    }

}
