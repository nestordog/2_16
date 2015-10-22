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

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.security.Future;
import ch.algotrader.entity.security.FutureImpl;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
@Repository // Required for exception translation
public class FutureDaoImpl extends AbstractDao<Future> implements FutureDao {

    public FutureDaoImpl(final SessionFactory sessionFactory) {

        super(FutureImpl.class, sessionFactory);
    }

    @Override
    public Future findByExpirationInclSecurityFamily(long futureFamilyId, Date expirationDate) {

        Validate.notNull(expirationDate, "expirationDate is null");

        return findUniqueCaching("Future.findByExpirationInclSecurityFamily", QueryType.BY_NAME, new NamedParam("futureFamilyId", futureFamilyId), new NamedParam("expirationDate", expirationDate));
    }

    @Override
    public List<Future> findByMinExpiration(long futureFamilyId, Date targetExpirationDate) {

        Validate.notNull(targetExpirationDate, "targetExpirationDate is null");

        return findCaching("Future.findByMinExpiration", QueryType.BY_NAME, new NamedParam("futureFamilyId", futureFamilyId), new NamedParam("targetExpirationDate", targetExpirationDate));
    }

    @Override
    public List<Future> findByMinExpiration(int limit, long futureFamilyId, Date targetExpirationDate) {

        Validate.notNull(targetExpirationDate, "targetExpirationDate is null");

        return findCaching("Future.findByMinExpiration", limit, QueryType.BY_NAME, new NamedParam("futureFamilyId", futureFamilyId), new NamedParam("targetExpirationDate", targetExpirationDate));
    }

    @Override
    public List<Future> findSubscribedFutures() {

        return findCaching("Future.findSubscribedFutures", QueryType.BY_NAME);
    }

    @Override
    public List<Future> findBySecurityFamily(long securityFamilyId) {

        return findCaching("Future.findBySecurityFamily", QueryType.BY_NAME, new NamedParam("securityFamilyId", securityFamilyId));
    }

    @Override
    public Future findByExpirationMonth(long futureFamilyId, Date expirationMonth) {

        Validate.notNull(expirationMonth, "Expiration month is null");

        return findUniqueCaching("Future.findByExpirationMonth", QueryType.BY_NAME, new NamedParam("futureFamilyId", futureFamilyId), new NamedParam("expirationMonth", expirationMonth));
    }

}
