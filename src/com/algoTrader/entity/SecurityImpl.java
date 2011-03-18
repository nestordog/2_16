package com.algoTrader.entity;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.util.ConfigurationUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.StrategyUtil;
import com.espertech.esper.event.bean.BeanEventBean;

public class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    private static Logger logger = MyLogger.getLogger(SecurityImpl.class.getName());

    private static Currency portfolioBaseCurrency = Currency.fromString(ConfigurationUtil.getBaseConfig().getString("portfolioBaseCurrency"));
    private static double initialMarginMarkup = ConfigurationUtil.getBaseConfig().getDouble("initialMarginMarkup");

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Tick getLastTick() {

        List<Map> events = ServiceLocator.commonInstance().getRuleService().getAllEvents(StrategyUtil.getStartedStrategyName(), "GET_LAST_TICK");

        // try to see if the rule GET_LAST_TICK has the tick
        for (Map event : events) {
            Integer securityId = (Integer) event.get("securityId");
            if (securityId.equals(getId())) {
                return (Tick) ((BeanEventBean) event.get("tick")).getUnderlying();
            }
        }

        // if we did not get the tick up to now go to the db an get the last tick
        Tick tick = ServiceLocator.commonInstance().getLookupService().getLastTick(getId());
        return tick;
    }

    /**
     * spread depends on the pricePerContract (i.e. spread should be the same
     * for 12.- à contractSize 10 as for 1.20 à contractSize 100)
     *
     * @return price per option
     */
    public double getDummyBid(double price) {

        if (getSecurityFamily().getSpreadSlope() == null || getSecurityFamily().getSpreadConstant() == null) {

            throw new RuntimeException("SpreadSlope and SpreadConstant have to be defined for " + getSymbol());
        }

        double pricePerContract = price * getSecurityFamily().getContractSize();
        double spread = pricePerContract * getSecurityFamily().getSpreadSlope() + getSecurityFamily().getSpreadConstant();
        return (pricePerContract - (spread / 2.0)) / getSecurityFamily().getContractSize();
    }

    /**
     * spread depends on the pricePerContract (i.e. spread should be the same
     * for 12.- à contractSize 10 as for 1.20 à contractSize 100)
     *
     * @return price per option
     */
    public double getDummyAsk(double price) {

        double pricePerContract = price * getSecurityFamily().getContractSize();
        double spread = pricePerContract * getSecurityFamily().getSpreadSlope() + getSecurityFamily().getSpreadConstant();
        return (pricePerContract + (spread / 2.0)) / getSecurityFamily().getContractSize();
    }

    public boolean isOnWatchlist() {

        return Hibernate.isInitialized(getWatchListItems()) && (getWatchListItems().size() != 0);
    }

    public void validateTick(Tick tick) {

        // do nothing, this method will be overwritten
    }

    public double getFXRate(Currency transactionCurrency) {

        return ServiceLocator.commonInstance().getLookupService().getForexRateDouble(getSecurityFamily().getCurrency(), transactionCurrency);
    }

    public double getFXRateBase() {

        return getFXRate(portfolioBaseCurrency);
    }

    public double getMargin() {

        Tick lastTick = getLastTick();

        double marginPerContract = 0;
        if (lastTick != null && lastTick.getCurrentValueDouble() > 0.0) {

            int contractSize = getSecurityFamily().getContractSize();
            marginPerContract = lastTick.getCurrentValueDouble() * contractSize / initialMarginMarkup;
        } else {
            logger.warn("no last tick available or currentValue to low to set margin on " + getSymbol());
        }
        return marginPerContract;
    }
}
