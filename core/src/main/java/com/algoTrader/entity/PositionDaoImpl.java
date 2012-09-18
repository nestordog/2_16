package com.algoTrader.entity;

import java.util.Map;

import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.ClosePositionVO;
import com.algoTrader.vo.ExpirePositionVO;
import com.algoTrader.vo.PositionVO;

@SuppressWarnings("unchecked")
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
        positionVO.setStrategyName(position.getStrategy().getName());
        positionVO.setCurrency(position.getSecurity().getSecurityFamily().getCurrency());
        positionVO.setMarketPrice(RoundUtil.getBigDecimal(position.getMarketPriceDouble(), scale));
        positionVO.setMarketValue(RoundUtil.getBigDecimal(position.getMarketValueDouble()));
        positionVO.setAveragePrice(RoundUtil.getBigDecimal(position.getAveragePriceDouble(), scale));
        positionVO.setCost(RoundUtil.getBigDecimal(position.getCostDouble()));
        positionVO.setUnrealizedPL(RoundUtil.getBigDecimal(position.getUnrealizedPLDouble()));
        positionVO.setExitValue(position.getExitValue() != null ? RoundUtil.getBigDecimal(position.getExitValue(), scale) : null);
        positionVO.setMaxLoss(RoundUtil.getBigDecimal(position.getMaxLossDouble()));
        positionVO.setMargin(position.getMaintenanceMargin());

        // add properties if any
        Map<String, Property> properties = position.getPropertiesInitialized();
        if (!properties.isEmpty()) {
            positionVO.setProperties(properties);
        }
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

        int scale = position.getSecurity().getSecurityFamily().getScale();

        closePositionVO.setSecurityId(position.getSecurity().getId());
        closePositionVO.setExitValue(position.getExitValue() != null ? RoundUtil.getBigDecimal(position.getExitValue(), scale) : null);
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
    public Position closePositionVOToEntity(ClosePositionVO closePositionVO) {

        throw new UnsupportedOperationException("closePositionVOToEntity is not implemented.");
    }

    @Override
    public Position expirePositionVOToEntity(ExpirePositionVO expirePositionVO) {

        throw new UnsupportedOperationException("expirePositionVOToEntity is not implemented.");
    }
}
