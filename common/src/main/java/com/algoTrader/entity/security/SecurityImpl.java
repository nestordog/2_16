package com.algoTrader.entity.security;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.marketData.Tick;
import com.algoTrader.enumeration.Currency;
import com.algoTrader.enumeration.Direction;
import com.algoTrader.service.RuleService;
import com.algoTrader.util.MyLogger;
import com.algoTrader.util.StrategyUtil;
import com.espertech.esper.event.WrapperEventBean;
import com.espertech.esper.event.bean.BeanEventBean;

public abstract class SecurityImpl extends Security {

    private static final long serialVersionUID = -6631052475125813394L;

    private static Logger logger = MyLogger.getLogger(SecurityImpl.class.getName());

    private static @Value("#{T(com.algoTrader.enumeration.Currency).fromString('${misc.portfolioBaseCurrency}')}") Currency portfolioBaseCurrency;
    private static @Value("${order.initialMarginMarkup}") double initialMarginMarkup;

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Tick getLastTick() {

        RuleService ruleService = ServiceLocator.instance().getRuleService();
        if (ruleService.isInitialized(StrategyUtil.getStartedStrategyName())) {
            List<Map> events = ruleService.getAllEvents(StrategyUtil.getStartedStrategyName(), "GET_LAST_TICK");

            // try to see if the rule GET_LAST_TICK has the tick
            for (Map event : events) {
                Integer securityId = (Integer) event.get("securityId");
                if (securityId.equals(getId())) {
                    Object obj = event.get("tick");
                    if (obj instanceof WrapperEventBean) {
                        return (Tick) ((WrapperEventBean) obj).getUnderlying();
                    } else {
                        return (Tick) ((BeanEventBean) obj).getUnderlying();
                    }
                }
            }
        }

        // if we did not get the tick up to now go to the db an get the last tick
        Tick tick = ServiceLocator.instance().getLookupService().getLastTick(getId());

        if (tick == null) {
            logger.warn("no last tick was found for " + this);
        }

        return tick;
    }

    @Override
    public boolean isSubscribed() {

        return Hibernate.isInitialized(getSubscriptions()) && (getSubscriptions().size() != 0);
    }

    @Override
    public double getFXRate(Currency transactionCurrency) {

        return ServiceLocator.instance().getLookupService().getForexRateDouble(getSecurityFamily().getCurrency(), transactionCurrency);
    }

    @Override
    public double getFXRateBase() {

        return getFXRate(portfolioBaseCurrency);
    }

    @Override
    public double getLeverage() {
        return 0;
    }

    /**
     * generic default margin
     */
    @Override
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

    @Override
    public String toString() {

        return getSymbol();
    }

    @Override
    public Component getComponentBySecurity(final Security security) {

        // find the component to the specified security
        return CollectionUtils.find(getComponents(), new Predicate<Component>() {
            @Override
            public boolean evaluate(Component component) {
                return security.equals(component.getSecurity());
            }
        });
    }

    @Override
    public long getComponentQuantity(final Security security) {

        Component component = getComponentBySecurity(security);

        if (component == null) {
            throw new IllegalArgumentException("no component exists for the defined master security");
        } else {
            return component.getQuantity();
        }
    }

    @Override
    public Direction getComponentDirection(final Security security) {

        long qty = getComponentQuantity(security);

        if (qty < 0) {
            return Direction.SHORT;
        } else if (qty > 0) {
            return Direction.LONG;
        } else {
            return Direction.FLAT;
        }
    }

    @Override
    public long getComponentTotalQuantity() {

        long quantity = 0;
        for (Component component : getComponents()) {
            quantity += component.getQuantity();
        }
        return quantity;
    }

    @Override
    public int getComponentCount() {
        return getComponents().size();
    }
}
