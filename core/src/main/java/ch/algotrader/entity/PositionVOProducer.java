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

import java.math.BigDecimal;
import java.util.Map;

import org.apache.commons.lang.Validate;

import ch.algotrader.entity.property.Property;
import ch.algotrader.hibernate.EntityConverter;
import ch.algotrader.util.RoundUtil;
import ch.algotrader.vo.PositionVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PositionVOProducer implements EntityConverter<Position, PositionVO> {

    public static final PositionVOProducer INSTANCE = new PositionVOProducer();

    @Override
    public PositionVO convert(final Position entity) {

        Validate.notNull(entity, "Position is null");

        PositionVO vo = new PositionVO();

        vo.setId(entity.getId());
        vo.setQuantity(entity.getQuantity());
        // No conversion for target.strategy (can't convert source.getStrategy():Strategy to String)
        vo.setCost(new BigDecimal(entity.getCost()));
        vo.setRealizedPL(new BigDecimal(entity.getRealizedPL()));
        vo.setExitValue(entity.getExitValue());

        int scale = entity.getSecurity().getSecurityFamily().getScale();

        vo.setSecurityId(entity.getSecurity().getId());
        vo.setName(entity.getSecurity().toString());
        vo.setStrategy(entity.getStrategy().toString());
        vo.setCurrency(entity.getSecurity().getSecurityFamily().getCurrency());
        vo.setMarketPrice(RoundUtil.getBigDecimal(entity.getMarketPrice(), scale));
        vo.setMarketValue(RoundUtil.getBigDecimal(entity.getMarketValue()));
        vo.setAveragePrice(RoundUtil.getBigDecimal(entity.getAveragePrice(), scale));
        vo.setCost(RoundUtil.getBigDecimal(entity.getCost()));
        vo.setUnrealizedPL(RoundUtil.getBigDecimal(entity.getUnrealizedPL()));
        vo.setRealizedPL(RoundUtil.getBigDecimal(entity.getRealizedPL()));
        vo.setExitValue(entity.getExitValue() != null ? entity.getExitValue().setScale(scale, BigDecimal.ROUND_HALF_UP) : null);
        vo.setMaxLoss(RoundUtil.getBigDecimal(entity.getMaxLoss()));
        vo.setMargin(entity.getMaintenanceMargin() != null ? entity.getMaintenanceMargin().setScale(scale, BigDecimal.ROUND_HALF_UP) : null);

        // add properties if any
        Map<String, Property> properties = entity.getPropsInitialized();
        if (!properties.isEmpty()) {
            vo.setProperties(properties);
        }

        return vo;
    }

}
