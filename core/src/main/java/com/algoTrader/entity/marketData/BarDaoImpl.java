package com.algoTrader.entity.marketData;

import org.hibernate.Hibernate;

import com.algoTrader.entity.security.Security;
import com.algoTrader.vo.BarVO;

/**
 * @see Bar
 */
public class BarDaoImpl extends BarDaoBase {

    @Override
    public void toBarVO(Bar bar, BarVO barVO) {

        super.toBarVO(bar, barVO);

        completeBarVO(bar, barVO);
    }

    @Override
    public BarVO toBarVO(final Bar bar) {

        BarVO rawBarVO = super.toBarVO(bar);

        completeBarVO(bar, rawBarVO);

        return rawBarVO;
    }

    private void completeBarVO(Bar bar, BarVO barVO) {

        barVO.setIsin(bar.getSecurity().getIsin());
    }

    @Override
    public Bar barVOToEntity(BarVO barVO) {

        Bar bar = new BarImpl();
        super.barVOToEntity(barVO, bar, true);

        Security security = getSecurityDao().findByIsinInclFamilyAndUnderlying(barVO.getIsin());

        // initialize the proxys
        Hibernate.initialize(security.getUnderlying());
        Hibernate.initialize(security.getSecurityFamily());

        bar.setSecurity(security);

        return bar;
    }
}
