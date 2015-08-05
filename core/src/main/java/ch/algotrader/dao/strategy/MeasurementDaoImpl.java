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
package ch.algotrader.dao.strategy;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;

import ch.algotrader.dao.AbstractDao;
import ch.algotrader.dao.NamedParam;
import ch.algotrader.entity.strategy.Measurement;
import ch.algotrader.entity.strategy.MeasurementImpl;
import ch.algotrader.enumeration.QueryType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
@Repository // Required for exception translation
public class MeasurementDaoImpl extends AbstractDao<Measurement> implements MeasurementDao {

    public MeasurementDaoImpl(final SessionFactory sessionFactory) {

        super(MeasurementImpl.class, sessionFactory);
    }

    @Override
    public Measurement findMeasurementByDate(String strategyName, String name, Date dateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(strategyName, "Name is empty");
        Validate.notNull(dateTime, "dateTime is null");

        return findUniqueCaching("Measurement.findMeasurementByDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("name", name), new NamedParam("dateTime", dateTime));
    }

    @Override
    public List<Measurement> findMeasurementsByMaxDate(String strategyName, String name, Date maxDateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(maxDateTime, "maxDateTime is null");

        return findCaching("Measurement.findMeasurementsByMaxDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("name", name), new NamedParam("maxDateTime", maxDateTime));
    }

    @Override
    public List<Measurement> findMeasurementsByMaxDate(int limit, String strategyName, String name, Date maxDateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(maxDateTime, "maxDateTime is null");

        return find("Measurement.findMeasurementsByMaxDate", limit, QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("name", name), new NamedParam("maxDateTime",
                maxDateTime));
    }

    @Override
    public List<Measurement> findAllMeasurementsByMaxDate(String strategyName, Date maxDateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(maxDateTime, "maxDateTime is null");

        return findCaching("Measurement.findAllMeasurementsByMaxDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("maxDateTime", maxDateTime));
    }

    @Override
    public List<Measurement> findMeasurementsByMinDate(String strategyName, String name, Date minDateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(minDateTime, "minDateTime is null");

        return findCaching("Measurement.findMeasurementsByMinDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("name", name), new NamedParam("minDateTime", minDateTime));
    }

    @Override
    public List<Measurement> findMeasurementsByMinDate(int limit, String strategyName, String name, Date minDateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notEmpty(name, "Name is empty");
        Validate.notNull(minDateTime, "minDateTime is null");

        return find("Measurement.findMeasurementsByMinDate", limit, QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("name", name), new NamedParam("minDateTime",
                minDateTime));
    }

    @Override
    public List<Measurement> findAllMeasurementsByMinDate(String strategyName, Date minDateTime) {

        Validate.notEmpty(strategyName, "Strategy name is empty");
        Validate.notNull(minDateTime, "minDateTime is null");

        return findCaching("Measurement.findAllMeasurementsByMinDate", QueryType.BY_NAME, new NamedParam("strategyName", strategyName), new NamedParam("minDateTime", minDateTime));
    }

}
