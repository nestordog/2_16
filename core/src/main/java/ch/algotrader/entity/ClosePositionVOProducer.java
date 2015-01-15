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
package ch.algotrader.entity;

import org.apache.commons.lang.Validate;

import ch.algotrader.hibernate.EntityConverter;
import ch.algotrader.vo.ClosePositionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ClosePositionVOProducer implements EntityConverter<Position, ClosePositionVO> {

    public static final ClosePositionVOProducer INSTANCE = new ClosePositionVOProducer();

    @Override
    public ClosePositionVO convert(final Position entity) {

        Validate.notNull(entity, "Position is null");

        ClosePositionVO vo = new ClosePositionVO();

        vo.setId(entity.getId());
        // No conversion for closePositionVO.strategy (can't convert position.getStrategy():Strategy to String)
        vo.setQuantity(entity.getQuantity());
        vo.setExitValue(entity.getExitValue());

        vo.setSecurityId(entity.getSecurity().getId());
        vo.setStrategy(entity.getStrategy().toString());
        vo.setExitValue(entity.getExitValue());
        vo.setDirection(entity.getDirection());

        return vo;
    }

}
