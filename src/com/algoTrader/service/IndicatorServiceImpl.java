package com.algoTrader.service;

import java.util.List;

import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.vo.MacdVO;
import com.algoTrader.vo.StochasticVO;

public class IndicatorServiceImpl extends com.algoTrader.service.IndicatorServiceBase {

    protected List<MacdVO> handleGetMACD() throws Exception {

        return EsperService.getAllEvents(RuleName.KEEP_MACD_VO, MacdVO.class);
    }

    protected List<StochasticVO> handleGetStochastic() throws Exception {

        return EsperService.getAllEvents(RuleName.KEEP_STOCHASTIC_VO, StochasticVO.class);
    }

}
