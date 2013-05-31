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
package ch.algotrader.entity.marketData;

import java.util.HashMap;
import java.util.Map;

import ch.algotrader.entity.marketData.BarImpl;
import ch.algotrader.util.metric.MetricsUtil;

import ch.algotrader.entity.marketData.Bar;
import ch.algotrader.entity.marketData.BarDaoBase;
import ch.algotrader.entity.security.Security;
import ch.algotrader.vo.BarVO;
import ch.algotrader.vo.RawBarVO;

@SuppressWarnings("unchecked")
/**
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
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

        long beforeRawToEntity = System.nanoTime();
        Bar bar = new BarImpl();
        super.rawBarVOToEntity(barVO, bar, true);
        long afterRawToEntity = System.nanoTime();

        // cache security id, as queries byIsin get evicted from cache whenever any change to security table happens
        long beforeGetSecurityId = System.nanoTime();
        String isin = barVO.getIsin();
        Integer securityId = this.securityIds.get(isin);
        if (securityId == null) {
            securityId = getSecurityDao().findSecurityIdByIsin(isin);
            this.securityIds.put(isin, securityId);
        }
        long afterGetSecurityId = System.nanoTime();

        // get the fully initialized security
        long beforeSecurityLookup = System.nanoTime();
        Security security = getSecurityDao().get(securityId);
        long afterSecurityLookup = System.nanoTime();

        long beforeInitialization = System.nanoTime();
        security.initialize();
        bar.setSecurity(security);
        long afterInitialization = System.nanoTime();

        MetricsUtil.account("MarketDataEventDao.rawToEntity", (afterRawToEntity - beforeRawToEntity));
        MetricsUtil.account("MarketDataEventDao.getSecurityId", (afterGetSecurityId - beforeGetSecurityId));
        MetricsUtil.account("MarketDataEventDao.securityLookup", (afterSecurityLookup - beforeSecurityLookup));
        MetricsUtil.account("MarketDataEventDao.initialization", (afterInitialization - beforeInitialization));

        return bar;
    }

    @Override
    public Bar barVOToEntity(BarVO barVO) {

        throw new UnsupportedOperationException("not implemented yet");
    }
}
