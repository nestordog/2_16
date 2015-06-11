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
package ch.algotrader.service.ftx;

import java.util.Map;

import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.ftx.FTXFixOrderMessageFactory;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.esper.Engine;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.service.OrderService;
import ch.algotrader.service.fix.fix44.Fix44OrderServiceImpl;
import quickfix.field.CumQty;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 *
 * @version $Revision$ $Date$
 */
public class FTXFixOrderServiceImpl extends Fix44OrderServiceImpl implements FTXFixOrderService {

    private static final long serialVersionUID = -4332474115892611530L;

    public FTXFixOrderServiceImpl(final FixAdapter fixAdapter,
                                  final OrderService orderService) {

        super(fixAdapter, orderService, new FTXFixOrderMessageFactory());
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {
    }

    @Override
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        Engine serverEngine = EngineLocator.instance().getServerEngine();
        if (serverEngine != null && serverEngine.isDeployed("OPEN_ORDER_WINDOW")) {
            String intId = order.getIntId();
            @SuppressWarnings("unchecked")
            Map<String, Long> map = (Map<String, Long>) serverEngine.executeSingelObjectQuery(
                    "select filledQuantity from OpenOrderWindow where intId = '" + intId + "'");
            Long filledQuantity = map.get("filledQuantity");
            if (filledQuantity != null) {
                replaceRequest.setDouble(CumQty.FIELD, filledQuantity);
            }
        }
    }

    @Override
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {
    }

    @Override
    public OrderServiceType getOrderServiceType() {

        return OrderServiceType.FTX_FIX;
    }
}
