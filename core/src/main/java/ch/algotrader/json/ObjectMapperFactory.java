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

package ch.algotrader.json;

import java.util.Date;

import ch.algotrader.entity.TransactionVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ch.algotrader.entity.trade.LimitOrderVO;
import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.entity.trade.StopLimitOrderVO;
import ch.algotrader.entity.trade.StopOrderVO;
import ch.algotrader.vo.marketData.MarketDataSubscriptionVO;

public class ObjectMapperFactory {

    public ObjectMapper create() {

        SimpleModule module = new SimpleModule();
        module.addDeserializer(Date.class, new InternalDateDeserializer());
        module.addSerializer(Date.class, new InternalDateSerializer());
        module.setMixInAnnotation(MarketOrderVO.class, MarketOrderVOMixIn.class);
        module.setMixInAnnotation(LimitOrderVO.class, LimitOrderVOMixIn.class);
        module.setMixInAnnotation(StopOrderVO.class, StopOrderVOMixIn.class);
        module.setMixInAnnotation(StopLimitOrderVO.class, StopLimitOrderVOMixIn.class);
        module.setMixInAnnotation(MarketDataSubscriptionVO.class, MarketDataSubscriptionVOMixIn.class);
        module.setMixInAnnotation(TransactionVO.class, TransactionVOMixin.class);
        return new ObjectMapper()
                .registerModule(module)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

}
