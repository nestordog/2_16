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
package ch.algotrader.dao;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Position;
import ch.algotrader.vo.OpenPositionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class OpenPositionVOProducer implements EntityConverter<Position, OpenPositionVO> {

    public static final OpenPositionVOProducer INSTANCE = new OpenPositionVOProducer();

    @Override
    public OpenPositionVO convert(final Position entity) {

        Validate.notNull(entity, "Position is null");

        OpenPositionVO openPositionVO = new OpenPositionVO();

        openPositionVO.setId(entity.getId());
        // No conversion for openPositionVO.strategy (can't convert position.getStrategy():Strategy to String)
        openPositionVO.setQuantity(entity.getQuantity());

        openPositionVO.setSecurityId(entity.getSecurity().getId());
        openPositionVO.setStrategy(entity.getStrategy().toString());
        openPositionVO.setDirection(entity.getDirection());

        return openPositionVO;
    }

}
