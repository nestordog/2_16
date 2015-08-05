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

package ch.algotrader.dao.marketData;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarImpl;
import ch.algotrader.enumeration.Duration;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class BarDaoImpl extends AbstractDao<Bar> implements BarDao {

    public BarDaoImpl(final SessionFactory sessionFactory) {

        super(BarImpl.class, sessionFactory);
    }

    @Override
    public List<Bar> findDailyBarsFromTicks(long securityId, Date minDate, Date maxDate) {

        Validate.notNull(minDate, "minDate is null");
        Validate.notNull(maxDate, "maxDate is null");

        return find("Bar.findDailyBarsFromTicks", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("minDate", minDate), new NamedParam("maxDate", maxDate));
    }

    @Override
    public List<Bar> findBarsBySecurityAndBarSize(int limit, long securityId, Duration barSize) {

        Validate.notNull(barSize, "barSize is null");

        return find("Bar.findBarsBySecurityAndBarSize", limit, QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("barSize", barSize));
    }

    @Override
    public List<Bar> findBarsBySecurityBarSizeAndMinDate(long securityId, Duration barSize, Date minDate) {

        Validate.notNull(barSize, "barSize is null");
        Validate.notNull(minDate, "minDate is null");

        return find("Bar.findBarsBySecurityBarSizeAndMinDate", QueryType.BY_NAME, new NamedParam("securityId", securityId), new NamedParam("barSize", barSize), new NamedParam("minDate", minDate));
    }

    @Override
    public List<Bar> findSubscribedByTimePeriodAndBarSize(Date minDate, Date maxDate, Duration barSize) {

        Validate.notNull(minDate, "minDate is null");
        Validate.notNull(maxDate, "maxDate is null");
        Validate.notNull(barSize, "barSize is null");

        return find("Bar.findSubscribedByTimePeriodAndBarSize", QueryType.BY_NAME, new NamedParam("minDate", minDate), new NamedParam("maxDate", maxDate), new NamedParam("barSize", barSize));
    }

    @Override
    public List<Bar> findSubscribedByTimePeriodAndBarSize(int limit, Date minDate, Date maxDate, Duration barSize) {

        Validate.notNull(minDate, "minDate is null");
        Validate.notNull(maxDate, "maxDate is null");
        Validate.notNull(barSize, "barSize is null");

        return find("Bar.findSubscribedByTimePeriodAndBarSize", limit, QueryType.BY_NAME, new NamedParam("minDate", minDate), new NamedParam("maxDate", maxDate), new NamedParam("barSize", barSize));
    }

}
