/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
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

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.security.SecurityReference;
import ch.algotrader.entity.security.SecurityReferenceImpl;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class SecurityReferenceDaoImpl extends AbstractDao<SecurityReference> implements SecurityReferenceDao {

    public SecurityReferenceDaoImpl(final SessionFactory sessionFactory) {

        super(SecurityReferenceImpl.class, sessionFactory);
    }

    @Override
    public SecurityReference findByOwnerAndName(long ownerSecurityId, String name) {

        Validate.notEmpty(name, "Name is empty");

        return findUnique("SecurityReference.findByOwnerAndName", QueryType.BY_NAME, new NamedParam("ownerSecurityId", ownerSecurityId), new NamedParam("name", name));
    }
}
