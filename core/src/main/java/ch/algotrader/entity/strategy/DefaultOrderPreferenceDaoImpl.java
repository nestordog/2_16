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
package ch.algotrader.entity.strategy;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class DefaultOrderPreferenceDaoImpl extends AbstractDao<DefaultOrderPreference> implements DefaultOrderPreferenceDao {

    public DefaultOrderPreferenceDaoImpl(final SessionFactory sessionFactory) {

        super(DefaultOrderPreferenceImpl.class, sessionFactory);
    }

    @Override
    public DefaultOrderPreference findByStrategyAndSecurityFamilyInclOrderPreference(String strategyName, int securityFamilyId) {

        Validate.notEmpty(strategyName, "Strategy name is empty");

        return findUnique("DefaultOrderPreference.findByStrategyAndSecurityFamilyInclOrderPreference", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam(
                "securityFamilyId", securityFamilyId));
    }

}
