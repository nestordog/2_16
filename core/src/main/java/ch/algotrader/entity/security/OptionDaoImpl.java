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
package ch.algotrader.entity.security;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.enumeration.OptionType;
import ch.algotrader.enumeration.QueryType;
import ch.algotrader.hibernate.AbstractDao;
import ch.algotrader.hibernate.NamedParam;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class OptionDaoImpl extends AbstractDao<Option> implements OptionDao {

    public OptionDaoImpl(final SessionFactory sessionFactory) {

        super(OptionImpl.class, sessionFactory);
    }

    @Override
    public List<Option> findByMinExpirationAndMinStrikeDistance(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType) {

        Validate.notNull(targetExpirationDate, "targetExpirationDate is null");
        Validate.notNull(underlyingSpot, "underlyingSpot is null");
        Validate.notNull(optionType, "optionType is null");

        return find("Option.findByMinExpirationAndMinStrikeDistance", limit, QueryType.BY_NAME, new NamedParam("underlyingId", underlyingId), new NamedParam("targetExpirationDate",
                targetExpirationDate), new NamedParam("underlyingSpot", underlyingSpot), new NamedParam("optionType", optionType));
    }

    @Override
    public List<Option> findByMinExpirationAndStrikeLimit(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType) {

        Validate.notNull(targetExpirationDate, "targetExpirationDate is null");
        Validate.notNull(underlyingSpot, "underlyingSpot is null");
        Validate.notNull(optionType, "optionType is null");

        return find("Option.findByMinExpirationAndStrikeLimit", limit, QueryType.BY_NAME, new NamedParam("underlyingId", underlyingId), new NamedParam("targetExpirationDate", targetExpirationDate),
                new NamedParam("underlyingSpot", underlyingSpot.doubleValue()), new NamedParam("optionType", optionType));
    }

    @Override
    public List<Option> findByMinExpirationAndMinStrikeDistanceWithTicks(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType, Date date) {

        Validate.notNull(targetExpirationDate, "targetExpirationDate is null");
        Validate.notNull(underlyingSpot, "underlyingSpot is null");
        Validate.notNull(optionType, "optionType is null");
        Validate.notNull(date, "Date is null");

        return find("Option.findByMinExpirationAndMinStrikeDistanceWithTicks", limit, QueryType.BY_NAME, new NamedParam("underlyingId", underlyingId), new NamedParam("targetExpirationDate",
                targetExpirationDate), new NamedParam("underlyingSpot", underlyingSpot), new NamedParam("optionType", optionType), new NamedParam("date", date));
    }

    @Override
    public List<Option> findByMinExpirationAndStrikeLimitWithTicks(int limit, int underlyingId, Date targetExpirationDate, BigDecimal underlyingSpot, OptionType optionType, Date date) {

        Validate.notNull(targetExpirationDate, "targetExpirationDate is null");
        Validate.notNull(underlyingSpot, "underlyingSpot is null");
        Validate.notNull(optionType, "optionType is null");
        Validate.notNull(date, "Date is null");

        return find("Option.findByMinExpirationAndStrikeLimitWithTicks", limit, QueryType.BY_NAME, new NamedParam("underlyingId", underlyingId), new NamedParam("targetExpirationDate",
                targetExpirationDate), new NamedParam("underlyingSpot", underlyingSpot.doubleValue()), new NamedParam("optionType", optionType), new NamedParam("date", date));
    }

    @Override
    public List<Option> findSubscribedOptions() {

        return find("Option.findSubscribedOptions", QueryType.BY_NAME);
    }

    @Override
    public List<Option> findBySecurityFamily(int securityFamilyId) {

        return find("Option.findBySecurityFamily", QueryType.BY_NAME, new NamedParam("securityFamilyId", securityFamilyId));
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<Date> findExpirationsByUnderlyingAndDate(int underlyingId, Date dateTime) {

        Validate.notNull(dateTime, "dateTime is null");

        return (List<Date>) findObjects(null, "Option.findExpirationsByUnderlyingAndDate", QueryType.BY_NAME, new NamedParam("underlyingId", underlyingId), new NamedParam("dateTime", dateTime));
    }

    @Override
    public Option findByExpirationStrikeAndType(int optionFamilyId, Date expirationDate, BigDecimal strike, OptionType type) {

        Validate.notNull(expirationDate, "Expiration date is null");
        Validate.notNull(strike, "Strike is null");
        Validate.notNull(type, "Type is null");

        return findUnique("Option.findByExpirationStrikeAndType", QueryType.BY_NAME, new NamedParam("optionFamilyId", optionFamilyId), new NamedParam("expirationDate", expirationDate),
                new NamedParam("strike", strike), new NamedParam("type", type));
    }

}
