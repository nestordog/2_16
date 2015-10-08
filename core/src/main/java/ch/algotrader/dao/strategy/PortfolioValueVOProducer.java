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

import org.apache.commons.lang.Validate;

import ch.algotrader.dao.EntityConverter;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PortfolioValueVOProducer implements EntityConverter<PortfolioValue, PortfolioValueVO> {

    public static final PortfolioValueVOProducer INSTANCE = new PortfolioValueVOProducer();

    @Override
    public PortfolioValueVO convert(final PortfolioValue entity) {

        Validate.notNull(entity, "PortfolioValue is null");

        PortfolioValueVO vo = new PortfolioValueVO();

        vo.setDateTime(entity.getDateTime());
        vo.setNetLiqValue(entity.getNetLiqValue());
        vo.setSecuritiesCurrentValue(entity.getSecuritiesCurrentValue());
        vo.setCashBalance(entity.getCashBalance());
        vo.setLeverage(entity.getLeverage());
        vo.setAllocation(entity.getAllocation());
        vo.setCashFlow(entity.getCashFlow());

        return vo;
    }

}
