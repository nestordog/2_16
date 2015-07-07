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

import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.entity.security.OptionFamily;
import ch.algotrader.entity.security.OptionFamilyImpl;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class OptionFamilyDaoImpl extends AbstractDao<OptionFamily> implements OptionFamilyDao {

    public OptionFamilyDaoImpl(final SessionFactory sessionFactory) {

        super(OptionFamilyImpl.class, sessionFactory);
    }

    @Override
    public OptionFamily findByUnderlying(long underlyingId) {

        return findUniqueCaching("OptionFamily.findByUnderlying", QueryType.BY_NAME, new NamedParam("underlyingId", underlyingId));
    }

}
