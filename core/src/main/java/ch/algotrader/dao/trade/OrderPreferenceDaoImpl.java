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
package ch.algotrader.dao.trade;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.trade.OrderPreference;
import ch.algotrader.entity.trade.OrderPreferenceImpl;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Repository // Required for exception translation
public class OrderPreferenceDaoImpl extends AbstractDao<OrderPreference> implements OrderPreferenceDao {

    public OrderPreferenceDaoImpl(final SessionFactory sessionFactory) {

        super(OrderPreferenceImpl.class, sessionFactory);
    }

    @Override
    public OrderPreference findByName(String name) {

        Validate.notEmpty(name, "Name is empty");

        return findUniqueCaching("OrderPreference.findByName", QueryType.BY_NAME, new NamedParam("name", name));
    }

}
