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
package ch.algotrader.dao;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.Position;
import ch.algotrader.vo.ClosePositionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class ClosePositionVOProducer implements EntityConverter<Position, ClosePositionVO> {

    public static final ClosePositionVOProducer INSTANCE = new ClosePositionVOProducer();

    @Override
    public ClosePositionVO convert(final Position entity) {

        Validate.notNull(entity, "Position is null");

        return new ClosePositionVO(
                entity.getId(), entity.getSecurity().getId(), entity.getStrategy().getName(), entity.getQuantity(), entity.getDirection());
    }

}
