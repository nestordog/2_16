/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algorader.entity;

import java.math.BigDecimal;
import java.util.Map;

import ch.algorader.util.RoundUtil;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.PositionDaoBase;
import com.algoTrader.entity.property.Property;
import com.algoTrader.vo.ClosePositionVO;
import com.algoTrader.vo.ExpirePositionVO;
import com.algoTrader.vo.OpenPositionVO;
import com.algoTrader.vo.PositionVO;

@SuppressWarnings("unchecked")
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class PositionDaoImpl extends PositionDaoBase {

    @Override
    public void toPositionVO(Position position, PositionVO positionVO) {

        super.toPositionVO(position, positionVO);

        completePositionVO(position, positionVO);
    }

    @Override
    public PositionVO toPositionVO(final Position position) {

        PositionVO positionVO = super.toPositionVO(position);

        completePositionVO(position, positionVO);

        return positionVO;
    }

    private void completePositionVO(Position position, PositionVO positionVO) {

        int scale = position.getSecurity().getSecurityFamily().getScale();
        positionVO.setSecurityId(position.getSecurity().getId());
        positionVO.setName(position.getSecurity().toString());
        positionVO.setStrategy(position.getStrategy().toString());
        positionVO.setCurrency(position.getSecurity().getSecurityFamily().getCurrency());
        positionVO.setMarketPrice(RoundUtil.getBigDecimal(position.getMarketPriceDouble(), scale));
        positionVO.setMarketValue(RoundUtil.getBigDecimal(position.getMarketValueDouble()));
        positionVO.setAveragePrice(RoundUtil.getBigDecimal(position.getAveragePriceDouble(), scale));
        positionVO.setCost(RoundUtil.getBigDecimal(position.getCostDouble()));
        positionVO.setUnrealizedPL(RoundUtil.getBigDecimal(position.getUnrealizedPLDouble()));
        positionVO.setRealizedPL(RoundUtil.getBigDecimal(position.getRealizedPLDouble()));
        positionVO.setExitValue(position.getExitValue() != null ? position.getExitValue().setScale(scale, BigDecimal.ROUND_HALF_UP) : null);
        positionVO.setMaxLoss(RoundUtil.getBigDecimal(position.getMaxLossDouble()));
        positionVO.setMargin(position.getMaintenanceMargin() != null ? position.getMaintenanceMargin().setScale(scale, BigDecimal.ROUND_HALF_UP) : null);

        // add properties if any
        Map<String, Property> properties = position.getPropertiesInitialized();
        if (!properties.isEmpty()) {
            positionVO.setProperties(properties);
        }
    }

    @Override
    public void toOpenPositionVO(Position position, OpenPositionVO openPositionVO) {

        super.toOpenPositionVO(position, openPositionVO);

        completeOpenPositionVO(position, openPositionVO);
    }

    @Override
    public OpenPositionVO toOpenPositionVO(final Position position) {

        OpenPositionVO openPositionVO = super.toOpenPositionVO(position);

        completeOpenPositionVO(position, openPositionVO);

        return openPositionVO;
    }

    private void completeOpenPositionVO(Position position, OpenPositionVO openPositionVO) {

        openPositionVO.setSecurityId(position.getSecurity().getId());
        openPositionVO.setStrategy(position.getStrategy().toString());

        openPositionVO.setDirection(position.getDirection());
    }

    @Override
    public void toClosePositionVO(Position position, ClosePositionVO closePositionVO) {

        super.toClosePositionVO(position, closePositionVO);

        completeClosePositionVO(position, closePositionVO);
    }

    @Override
    public ClosePositionVO toClosePositionVO(final Position position) {

        ClosePositionVO closePositionVO = super.toClosePositionVO(position);

        completeClosePositionVO(position, closePositionVO);

        return closePositionVO;
    }

    private void completeClosePositionVO(Position position, ClosePositionVO closePositionVO) {

        closePositionVO.setSecurityId(position.getSecurity().getId());
        closePositionVO.setStrategy(position.getStrategy().toString());
        closePositionVO.setExitValue(position.getExitValue());
        closePositionVO.setDirection(position.getDirection());
    }

    @Override
    public void toExpirePositionVO(Position position, ExpirePositionVO expirePositionVO) {

        super.toExpirePositionVO(position, expirePositionVO);

        completeExpirePositionVO(position, expirePositionVO);
    }

    @Override
    public ExpirePositionVO toExpirePositionVO(final Position position) {

        ExpirePositionVO expirePositionVO = super.toExpirePositionVO(position);

        completeExpirePositionVO(position, expirePositionVO);

        return expirePositionVO;
    }

    private void completeExpirePositionVO(Position position, ExpirePositionVO expirePositionVO) {

        expirePositionVO.setSecurityId(position.getSecurity().getId());
        expirePositionVO.setDirection(position.getDirection());
    }

    @Override
    public Position positionVOToEntity(PositionVO positionVO) {

        throw new UnsupportedOperationException("positionVOToEntity ist not implemented.");
    }

    @Override
    public Position openPositionVOToEntity(OpenPositionVO openPositionVO) {

        throw new UnsupportedOperationException("openPositionVOToEntity is not implemented.");
    }

    @Override
    public Position closePositionVOToEntity(ClosePositionVO closePositionVO) {

        throw new UnsupportedOperationException("closePositionVOToEntity is not implemented.");
    }

    @Override
    public Position expirePositionVOToEntity(ExpirePositionVO expirePositionVO) {

        throw new UnsupportedOperationException("expirePositionVOToEntity is not implemented.");
    }
}
