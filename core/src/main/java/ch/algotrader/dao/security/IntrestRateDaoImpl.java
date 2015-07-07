/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
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
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.dao.security;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.entity.security.IntrestRate;
import ch.algotrader.entity.security.IntrestRateImpl;
import ch.algotrader.enumeration.Currency;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class IntrestRateDaoImpl extends AbstractDao<IntrestRate> implements IntrestRateDao {

    public IntrestRateDaoImpl(final SessionFactory sessionFactory) {

        super(IntrestRateImpl.class, sessionFactory);
    }

    @Override
    public IntrestRate findByCurrencyAndDuration(Currency currency, Duration duration) {

        Validate.notNull(currency, "Currency is null");
        Validate.notNull(duration, "Duration is null");

        return findUniqueCaching("IntrestRate.findByCurrencyAndDuration", QueryType.BY_NAME, new NamedParam("currency", currency), new NamedParam("duration", duration));
    }
}
