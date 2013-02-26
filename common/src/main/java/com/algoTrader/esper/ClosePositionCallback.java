package com.algoTrader.esper;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.ClosePositionVO;

public abstract class ClosePositionCallback {

    private static Logger logger = MyLogger.getLogger(ClosePositionCallback.class.getName());

    public void update(ClosePositionVO positionVO) throws Exception {

        // get the statement alias based on all security ids
        String alias = "ON_CLOSE_POSITION_" + positionVO.getSecurityId();

        // undeploy the statement
        EsperManager.undeployStatement(positionVO.getStrategy(), alias);

        long startTime = System.nanoTime();
        logger.debug("onClosePosition start " + positionVO.getSecurityId());

        // call orderCompleted
        onClosePosition(positionVO);

        logger.debug("onClosePosition end " + positionVO.getSecurityId());

        MetricsUtil.accountEnd("ClosePositionCallback." + positionVO.getStrategy(), startTime);
    }

    public abstract void onClosePosition(ClosePositionVO positionVO) throws Exception;
}
