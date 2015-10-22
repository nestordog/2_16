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

package ch.algotrader.dao.security;

import org.hibernate.SessionFactory;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.entity.security.BondFamily;
import ch.algotrader.entity.security.BondFamilyImpl;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class BondFamilyDaoImpl extends AbstractDao<BondFamily> implements BondFamilyDao {

    public BondFamilyDaoImpl(final SessionFactory sessionFactory) {

        super(BondFamilyImpl.class, sessionFactory);
    }

}
