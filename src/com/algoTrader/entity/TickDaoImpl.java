package com.algoTrader.entity;

import java.util.ArrayList;
import java.util.List;

import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.util.RoundUtil;
import com.algoTrader.vo.TickVO;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.SafeIterator;

public class TickDaoImpl extends TickDaoBase {

    public void toTickVO(Tick tick, TickVO tickVO) {

        super.toTickVO(tick, tickVO);

        completeTickVO(tick, tickVO);
    }

    public TickVO toTickVO(final Tick tick) {

        TickVO tickVO = super.toTickVO(tick);

        completeTickVO(tick, tickVO);

        return tickVO;
    }

    private void completeTickVO(Tick tick, TickVO tickVO) {

        tickVO.setSymbol(tick.getSecurity().getSymbol());
        tickVO.setMidpoint(RoundUtil.getBigDecimal((tick.getBid().doubleValue() + tick.getAsk().doubleValue()) / 2.0));
    }

    public Tick tickVOToEntity(TickVO tickVO) {

        throw new UnsupportedOperationException("tickVOToEntity not yet implemented.");

    }

    protected synchronized List<Tick> handleGetLastTicks() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.GET_LAST_TICK);

        List<Tick> ticks = new ArrayList<Tick>();
        if (statement != null && statement.isStarted()) {
            SafeIterator<EventBean> it = statement.safeIterator();
            try {
                while (it.hasNext()) {
                    EventBean bean = it.next();
                    ticks.add((Tick)bean.get("tick"));
                }
            } finally {
                it.close();
            }
        }
        return ticks;
    }

    protected synchronized boolean handleHasLastTicks() {

        return (EsperService.getLastEvent(RuleName.GET_LAST_TICK) != null);
    }
}
