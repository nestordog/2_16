package com.algoTrader.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.algoTrader.enumeration.RuleName;
import com.algoTrader.util.EsperService;
import com.algoTrader.vo.TickVO;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;

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
    }

    public Tick tickVOToEntity(TickVO tickVO) {

        throw new UnsupportedOperationException("tickVOToEntity not yet implemented.");

    }

    protected List<Tick> handleGetLastTicks() throws Exception {

        EPStatement statement = EsperService.getStatement(RuleName.GET_LAST_TICK);

        List<Tick> ticks = new ArrayList<Tick>();
        if (statement != null && statement.isStarted()) {
            for (Iterator<EventBean> it = statement.iterator(); it.hasNext(); ) {
                EventBean bean = it.next();
                ticks.add((Tick)bean.get("tick"));
            }
        }
        return ticks;
    }
}
