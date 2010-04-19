package com.algoTrader.entity;

import com.algoTrader.vo.PositionVO;

public class PositionDaoImpl extends PositionDaoBase {

    public void toPositionVO(Position position, PositionVO positionVO) {

        super.toPositionVO(position, positionVO);

        completePositionVO(position, positionVO);
    }

    public PositionVO toPositionVO(final Position position) {

        PositionVO positionVO = super.toPositionVO(position);

        completePositionVO(position, positionVO);

        return positionVO;
    }

    private void completePositionVO(Position position, PositionVO positionVO) {

        positionVO.setSymbol(position.getSecurity().getSymbol());
        positionVO.setCurrency(position.getAccount().getCurrency());
        positionVO.setCurrentValue(position.getSecurity().getLastTick().getCurrentValue());
    }

    public Position positionVOToEntity(PositionVO positionVO) {

        throw new UnsupportedOperationException("positionVOToEntity not yet implemented.");
    }
}
