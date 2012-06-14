package com.algoTrader.entity.marketData;

import java.util.HashMap;
import java.util.Map;

import com.algoTrader.entity.security.Security;
import com.algoTrader.vo.BarVO;
import com.algoTrader.vo.RawBarVO;

@SuppressWarnings("unchecked")
public class BarDaoImpl extends BarDaoBase {

    Map<String, Integer> securityIds = new HashMap<String, Integer>();

    @Override
    public void toRawBarVO(Bar bar, RawBarVO barVO) {

        super.toRawBarVO(bar, barVO);

        completeRawBarVO(bar, barVO);
    }

    @Override
    public RawBarVO toRawBarVO(final Bar bar) {

        RawBarVO rawBarVO = super.toRawBarVO(bar);

        completeRawBarVO(bar, rawBarVO);

        return rawBarVO;
    }

    private void completeRawBarVO(Bar bar, RawBarVO barVO) {

        barVO.setIsin(bar.getSecurity().getIsin());
    }

    @Override
    public void toBarVO(Bar bar, BarVO barVO) {

        super.toBarVO(bar, barVO);

        completeBarVO(bar, barVO);
    }

    @Override
    public BarVO toBarVO(final Bar bar) {

        BarVO barVO = super.toBarVO(bar);

        completeBarVO(bar, barVO);

        return barVO;
    }

    private void completeBarVO(Bar bar, BarVO barVO) {

        barVO.setSecurityId(bar.getSecurity().getId());
    }

    @Override
    public Bar rawBarVOToEntity(RawBarVO barVO) {

        Bar bar = new BarImpl();
        super.rawBarVOToEntity(barVO, bar, true);

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        String isin = barVO.getIsin();
        Integer securityId = this.securityIds.get(isin);
        if (securityId == null) {
            securityId = getSecurityDao().findSecurityIdByIsin(isin);
            this.securityIds.put(isin, securityId);
        }

        // get the fully initialized security
        Security security = getSecurityDao().get(securityId);
        security.initialize();

        bar.setSecurity(security);

        return bar;
    }

    @Override
    public Bar barVOToEntity(BarVO barVO) {

        throw new UnsupportedOperationException("not implemented yet");
    }
}
