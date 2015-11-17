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
package ch.algotrader.service.lmax;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.adapter.fix.DropCopyAllocationVO;
import ch.algotrader.adapter.fix.DropCopyAllocator;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.HibernateInitializer;
import ch.algotrader.dao.security.SecurityDao;
import ch.algotrader.dao.strategy.StrategyDao;
import ch.algotrader.entity.Account;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.strategy.Strategy;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class LMAXDropCopyAllocationServiceImpl implements DropCopyAllocator {

    private final SecurityDao securityDao;
    private final AccountDao accountDao;
    private final StrategyDao strategyDao;

    public LMAXDropCopyAllocationServiceImpl(
            final SecurityDao securityDao,
            final AccountDao accountDao,
            final StrategyDao strategyDao) {

        Validate.notNull(securityDao, "SecurityDao is null");
        Validate.notNull(accountDao, "AccountDao is null");
        Validate.notNull(strategyDao, "strategyDao is null");

        this.securityDao = securityDao;
        this.accountDao = accountDao;
        this.strategyDao = strategyDao;
    }

    @Override
    public DropCopyAllocationVO allocate(final String lmaxid, final String extAccount) {

        Security security = this.securityDao.findByLmaxid(lmaxid);
        if (security != null) {
            security.initializeSecurityFamily(HibernateInitializer.INSTANCE);
        }
        Account account = this.accountDao.findByExtAccount(extAccount);
        Strategy strategy = this.strategyDao.findServer();

        return new DropCopyAllocationVO(security, account, strategy);
    }

}
