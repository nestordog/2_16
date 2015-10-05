/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.adapter.tt;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import ch.algotrader.adapter.fix.fix42.GenericFix42OrderMessageHandler;
import ch.algotrader.esper.Engine;
import ch.algotrader.ordermgmt.OrderRegistry;
import quickfix.FieldNotFound;
import quickfix.field.ExecTransType;
import quickfix.fix42.ExecutionReport;

/**
 * Trading Technology specific FIX/4.2 order message handler.
 *
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class TTFixOrderMessageHandler extends GenericFix42OrderMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(TTFixOrderMessageHandler.class);

    public TTFixOrderMessageHandler(final OrderRegistry orderRegistry, final Engine serverEngine) {
        super(orderRegistry, serverEngine);
    }


    @Override
    protected boolean discardReport(final ExecutionReport executionReport) throws FieldNotFound {

        if (executionReport.isSetExecTransType()) {
            ExecTransType execTransType = executionReport.getExecTransType();
            if (execTransType.getValue() == ExecTransType.STATUS) {
                if (LOGGER.isInfoEnabled()) {
                    String orderIntId = executionReport.getClOrdID().getValue();
                    LOGGER.info("Working order at the TT side: {}", orderIntId);
                }
                return true;
            }
        }
        return super.discardReport(executionReport);
    }

}
