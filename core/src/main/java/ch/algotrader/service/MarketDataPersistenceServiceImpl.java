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
package ch.algotrader.service;

import org.apache.commons.lang.Validate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import ch.algotrader.dao.marketData.TickDao;
import ch.algotrader.entity.marketData.Tick;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Transactional(propagation = Propagation.SUPPORTS)
public class MarketDataPersistenceServiceImpl implements MarketDataPersistenceService {

    private final TickDao tickDao;

    public MarketDataPersistenceServiceImpl(final TickDao tickDao) {

        Validate.notNull(tickDao, "TickDao is null");

        this.tickDao = tickDao;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void persistTick(final Tick tick) {

        Validate.notNull(tick, "Tick is null");
        // write the tick to the DB (even if not valid)
        this.tickDao.save(tick);
    }

}
