/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service.ftx;

import ch.algotrader.adapter.ExternalSessionStateHolder;
import ch.algotrader.adapter.fix.FixAdapter;
import ch.algotrader.adapter.ftx.FTXFixOrderMessageFactory;
import ch.algotrader.config.CommonConfig;
import ch.algotrader.dao.AccountDao;
import ch.algotrader.dao.trade.OrderDao;
import ch.algotrader.entity.trade.ExecutionStatusVO;
import ch.algotrader.entity.trade.SimpleOrder;
import ch.algotrader.enumeration.OrderServiceType;
import ch.algotrader.enumeration.SimpleOrderType;
import ch.algotrader.enumeration.TIF;
import ch.algotrader.ordermgmt.OrderRegistry;
import ch.algotrader.service.OrderPersistenceService;
import ch.algotrader.service.fix.fix44.Fix44OrderService;
import ch.algotrader.service.fix.fix44.Fix44OrderServiceImpl;
import quickfix.field.CumQty;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelReplaceRequest;
import quickfix.fix44.OrderCancelRequest;

/**
 * @author <a href="mailto:okalnichevski@algotrader.ch">Oleg Kalnichevski</a>
 */
public class FTXFixOrderServiceImpl extends Fix44OrderServiceImpl implements Fix44OrderService {

    private final OrderRegistry orderRegistry;

    public FTXFixOrderServiceImpl(
            final FixAdapter fixAdapter,
            final ExternalSessionStateHolder stateHolder,
            final OrderRegistry orderRegistry,
            final OrderPersistenceService orderPersistenceService,
            final OrderDao orderDao,
            final AccountDao accountDao,
            final CommonConfig commonConfig) {

        super(OrderServiceType.FTX_FIX.name(), fixAdapter, stateHolder, new FTXFixOrderMessageFactory(),
                orderRegistry, orderPersistenceService, orderDao, accountDao, commonConfig);
        this.orderRegistry = orderRegistry;
    }

    @Override
    public void prepareSendOrder(SimpleOrder order, NewOrderSingle newOrder) {
    }

    @Override
    public void prepareModifyOrder(SimpleOrder order, OrderCancelReplaceRequest replaceRequest) {

        String intId = order.getIntId();
        ExecutionStatusVO execStatus = this.orderRegistry.getStatusByIntId(intId);
        if (execStatus != null) {
            replaceRequest.setDouble(CumQty.FIELD, execStatus.getFilledQuantity());
        }
    }

    @Override
    public void prepareCancelOrder(SimpleOrder order, OrderCancelRequest cancelRequest) {
    }

    @Override
    public TIF getDefaultTIF(final SimpleOrderType type) {
        return TIF.GTC;
    }

}
