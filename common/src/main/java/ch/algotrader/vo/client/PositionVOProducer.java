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
package ch.algotrader.vo.client;

import java.util.Map;

import org.apache.commons.lang.Validate;

import ch.algotrader.dao.EntityConverter;
import ch.algotrader.entity.Position;
import ch.algotrader.entity.marketData.MarketDataEventVO;
import ch.algotrader.entity.property.Property;
import ch.algotrader.service.MarketDataCache;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class PositionVOProducer implements EntityConverter<Position, PositionVO> {

    private final MarketDataCache marketDataCache;

    public PositionVOProducer(final MarketDataCache marketDataCache) {
        this.marketDataCache = marketDataCache;
    }

    @Override
    public PositionVO convert(final Position entity) {

        Validate.notNull(entity, "Position is null");

        MarketDataEventVO marketDataEvent = this.marketDataCache.getCurrentMarketDataEvent(entity.getSecurity().getId());

        PositionVO vo = new PositionVO();

        vo.setId(entity.getId());
        vo.setQuantity(entity.getQuantity());
        vo.setSecurityId(entity.getSecurity().getId());
        vo.setName(entity.getSecurity().toString());
        vo.setStrategy(entity.getStrategy().toString());
        vo.setCurrency(entity.getSecurity().getSecurityFamily().getCurrency());
        vo.setMarketPrice(entity.getMarketPrice(marketDataEvent));
        vo.setMarketValue(entity.getMarketValue(marketDataEvent));
        vo.setAveragePrice(entity.getAveragePrice());
        vo.setCost(entity.getCost());
        vo.setUnrealizedPL(entity.getUnrealizedPL(marketDataEvent));
        vo.setRealizedPL(entity.getRealizedPL());

        // add properties if any
        Map<String, Property> properties = entity.getProps();
        if (!properties.isEmpty()) {
            vo.setProperties(properties);
        }

        return vo;
    }

}
