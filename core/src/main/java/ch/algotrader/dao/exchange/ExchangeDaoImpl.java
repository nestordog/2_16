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
package ch.algotrader.dao.exchange;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.exchange.Exchange;
import ch.algotrader.entity.exchange.ExchangeImpl;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class ExchangeDaoImpl extends AbstractDao<Exchange> implements ExchangeDao {

    public ExchangeDaoImpl(final SessionFactory sessionFactory) {

        super(ExchangeImpl.class, sessionFactory);
    }

    @Override
    public Exchange findByName(String name) {

        Validate.notEmpty(name, "Exchange name is empty");

        return findUniqueCaching("Exchange.findByName", QueryType.BY_NAME, new NamedParam("name", name));
    }

}
