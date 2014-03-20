package ${package};

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.EngineLocator;

/**
 * This class starts the strategy in Live Trading Mode
 */
public class ${serviceName}Starter {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.CLIENT_BEAN_REFERENCE_LOCATION);

        ${serviceName}Service ${artifactId}Service = ServiceLocator.instance().getService("${artifactId}Service", ${serviceName}Service.class);
        String strategyName = ${artifactId}Service.getStrategyName();

        EngineLocator.instance().initEngine(strategyName);

        EngineLocator.instance().getEngine(strategyName).deployInitModules();

        EngineLocator.instance().getEngine(strategyName).setInternalClock(true);

        EngineLocator.instance().getEngine(strategyName).deployRunModules();

        ServiceLocator.instance().getSubscriptionService().initMarketDataEventSubscriptions();
    }
}
