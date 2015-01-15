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

import java.util.List;

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
public class StockDaoImpl extends AbstractDao<Stock> implements StockDao {

    public StockDaoImpl(final SessionFactory sessionFactory) {

        super(StockImpl.class, sessionFactory);
    }

    @Override
    public List<Stock> findBySectory(String code) {

        Validate.notEmpty(code, "Code is empty");

        return find("Stock.findBySectory", QueryType.BY_NAME, new NamedParam("code", code));
    }

    @Override
    public List<Stock> findByIndustryGroup(String code) {

        Validate.notEmpty(code, "Code is empty");

        return find("Stock.findByIndustryGroup", QueryType.BY_NAME, new NamedParam("code", code));
    }

    @Override
    public List<Stock> findByIndustry(String code) {

        Validate.notEmpty(code, "Code is empty");

        return find("Stock.findByIndustry", QueryType.BY_NAME, new NamedParam("code", code));
    }

    @Override
    public List<Stock> findBySubIndustry(String code) {

        Validate.notEmpty(code, "Code is empty");

        return find("Stock.findBySubIndustry", QueryType.BY_NAME, new NamedParam("code", code));
    }

    @Override
    public List<Stock> findStocksBySecurityFamily(int securityFamilyId) {

        return find("Stock.findStocksBySecurityFamily", QueryType.BY_NAME, new NamedParam("securityFamilyId", securityFamilyId));
    }

}
