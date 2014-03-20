package ${package};

import java.math.BigDecimal;
import ch.algotrader.service.StrategyServiceImpl;
import org.springframework.beans.factory.annotation.Value;

/**
 * The main service class of the ${serviceName} Strategy
 */
public class ${serviceName}Service extends StrategyServiceImpl {

    // configuration variables
    private @Value("${strategyName}") String strategyName;

    public void openPosition(String strategyName, int securityId, BigDecimal price) {

        //TODO: implement logic
    }

    public String getStrategyName() {

        return this.strategyName;
    }
}
