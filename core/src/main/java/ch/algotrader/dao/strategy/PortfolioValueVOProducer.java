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
package ch.algotrader.dao.strategy;

import org.apache.commons.lang.Validate;

import ch.algotrader.dao.EntityConverter;
import ch.algotrader.entity.strategy.PortfolioValue;
import ch.algotrader.vo.PortfolioValueVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class PortfolioValueVOProducer implements EntityConverter<PortfolioValue, PortfolioValueVO> {

    public static final PortfolioValueVOProducer INSTANCE = new PortfolioValueVOProducer();

    @Override
    public PortfolioValueVO convert(final PortfolioValue entity) {

        Validate.notNull(entity, "PortfolioValue is null");

        PortfolioValueVO vo = new PortfolioValueVO(0l, //
                entity.getDateTime(), //
                entity.getNetLiqValue(), //
                entity.getMarketValue(), //
                entity.getRealizedPL(), //
                entity.getUnrealizedPL(), //
                entity.getCashBalance(), //
                entity.getOpenPositions(), //
                entity.getLeverage(), //
                entity.getCashFlow(), //
                entity.getStrategy().getId());

        return vo;
    }

}
