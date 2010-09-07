package com.algoTrader.subscriber;

import java.math.BigDecimal;

import com.algoTrader.ServiceLocator;

public class OpenPositionSubscriber {

    public void update(int securityId, BigDecimal currentValue, BigDecimal underlayingSpot, BigDecimal stockOptionSettlement, BigDecimal underlayingSettlement, double volatility) {

        ServiceLocator.instance().getActionService().openPosition(securityId, currentValue, underlayingSpot, volatility, stockOptionSettlement, underlayingSettlement);
    }
}
