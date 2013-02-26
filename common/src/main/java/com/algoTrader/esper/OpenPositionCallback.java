package com.algoTrader.esper;

import org.apache.log4j.Logger;

import com.algoTrader.util.MyLogger;
import com.algoTrader.util.metric.MetricsUtil;
import com.algoTrader.vo.OpenPositionVO;

public abstract class OpenPositionCallback {

    private static Logger logger = MyLogger.getLogger(OpenPositionCallback.class.getName());

    public void update(OpenPositionVO positionVO) throws Exception {

        // get the statement alias based on all security ids
        String alias = "ON_OPEN_POSITION_" + positionVO.getSecurityId();

        // undeploy the statement
        EsperManager.undeployStatement(positionVO.getStrategy(), alias);

        long startTime = System.nanoTime();
        logger.debug("onOpenPosition start " + positionVO.getSecurityId());

        // call orderCompleted
        onOpenPosition(positionVO);

        logger.debug("onOpenPosition end " + positionVO.getSecurityId());

        MetricsUtil.accountEnd("OpenPositionCallback." + positionVO.getStrategy(), startTime);
    }

    public abstract void onOpenPosition(OpenPositionVO positionVO) throws Exception;
}
