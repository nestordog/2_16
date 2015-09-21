package ${package};

import java.math.BigDecimal;
import ch.algotrader.service.StrategyService;
import org.springframework.beans.factory.annotation.Value;

/**
 * The main service class of the ${serviceName} Strategy
 */
public class ${serviceName}Service extends StrategyService {

    // configuration variables
    private @Value("${strategyName}") String strategyName;

    public void openPosition(String strategyName, long securityId, BigDecimal price) {

        //TODO: implement logic
    }

    public String getStrategyName() {

        return this.strategyName;
    }

    @Override
    public void onInit(final LifecycleEventVO event) {
        switch (event.getOperationMode()) {
            case SIMULATION:
                break;
            case REAL_TIME:
                break;
        }
    }

    @Override
    public void onPrefeed(final LifecycleEventVO event) {
        switch (event.getOperationMode()) {
            case REAL_TIME:
                break;
        }
    }

}
