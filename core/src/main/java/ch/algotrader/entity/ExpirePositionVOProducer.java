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
import ch.algotrader.vo.ExpirePositionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ExpirePositionVOProducer implements EntityConverter<Position, ExpirePositionVO> {

    public static final ExpirePositionVOProducer INSTANCE = new ExpirePositionVOProducer();

    @Override
    public ExpirePositionVO convert(final Position entity) {

        Validate.notNull(entity, "Position is null");

        ExpirePositionVO vo = new ExpirePositionVO();

        vo.setId(entity.getId());
        // No conversion for vo.strategy (can't convert position.getStrategy():Strategy to String)
        vo.setQuantity(entity.getQuantity());

        vo.setSecurityId(entity.getSecurity().getId());
        vo.setDirection(entity.getDirection());
        return vo;
    }

}
