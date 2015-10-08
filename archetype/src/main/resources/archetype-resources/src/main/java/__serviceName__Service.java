package ${package};

import ch.algotrader.config.ConfigParams;
import ch.algotrader.entity.trade.MarketOrderVO;
import ch.algotrader.entity.trade.MarketOrderVOBuilder;
import ch.algotrader.enumeration.Side;
import ch.algotrader.service.StrategyService;
import ch.algotrader.vo.LifecycleEventVO;

/**
 * The main service class of the ${serviceName} Strategy
 */
public class ${serviceName}Service extends StrategyService {

    private final long accountId;
    private final long securityId;
    private final long orderQuantity;

    public ${serviceName}Service(ConfigParams params) {
        this.accountId = params.getInteger("accountId");
        this.securityId = params.getInteger("securityId");
        this.orderQuantity = params.getInteger("orderQuantity");
    }

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
