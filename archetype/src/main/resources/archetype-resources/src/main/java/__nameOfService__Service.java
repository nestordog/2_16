package ${package};

import java.math.BigDecimal;
import org.springframework.jmx.export.annotation.ManagedResource;
import ch.algotrader.service.StrategyServiceImpl;
import org.springframework.beans.factory.annotation.Value;

/**
 * The main service class of the ${nameOfService} Strategy
 */
@ManagedResource(objectName = "${package}:name=${nameOfService}Service")
public class ${nameOfService}Service extends StrategyServiceImpl {

    // configuration variables
    private @Value("${strategyName}") String strategyName;

    public void openPosition(String strategyName, int securityId, BigDecimal price) {

        //TODO: implement logic
    }

    public String getStrategyName() {

        return this.strategyName;
    }
}
