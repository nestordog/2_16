package ${package};

import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.entity.trade.MarketOrderVOBuilder;
import ch.algotrader.enumeration.Side;
import ch.algotrader.service.StrategyService;
import ch.algotrader.vo.LifecycleEventVO;

/**
 * The main service class of the ${serviceName} Strategy
 */
public class ${serviceName}Service extends StrategyService {

    private @Value("#{@${name}ConfigParams.accountId}") long accountId;
    private @Value("#{@${name}ConfigParams.securityId}") long securityId;
    private @Value("#{@${name}ConfigParams.orderQuantity}") long orderQuantity;

    public void sendOrder(Side side) {

        MarketOrderVO order = MarketOrderVOBuilder.create()
            .setStrategyId(getStrategy().getId())
            .setAccountId(this.accountId)
            .setSecurityId(this.securityId)
            .setQuantity(this.orderQuantity)
            .setSide(side)
            .build();

        getOrderService().sendOrder(order);
    }

    @Override
    public void onStart(final LifecycleEventVO event) {
        getSubscriptionService().subscribeMarketDataEvent(getStrategyName(), this.securityId);
    }

}
