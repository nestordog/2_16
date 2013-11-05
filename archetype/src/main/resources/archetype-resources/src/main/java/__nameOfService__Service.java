package ${package};

import java.math.BigDecimal;
import org.springframework.jmx.export.annotation.ManagedResource;
import ch.algotrader.service.StrategyServiceImpl;

/**
 * The main service class of the ${nameOfService} Strategy
 */
@ManagedResource(objectName = "${package}:name=${nameOfService}Service")
public class ${nameOfService}Service extends StrategyServiceImpl {

    public void openPosition(String strategyName, int securityId, BigDecimal price) {

        //TODO: Generated Method
    }
}
