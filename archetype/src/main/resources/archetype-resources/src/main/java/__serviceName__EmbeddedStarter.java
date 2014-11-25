package ${package};

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.starter.ServerStarter;

/**
 * This class starts the strategy in Live Trading Mode in a single VM
 */
public class ${serviceName}EmbeddedStarter extends ServerStarter  {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.EMBEDDED_BEAN_REFERENCE_LOCATION);

        startServer();

        ${serviceName}Service ${serviceName.toLowerCase()}Service = ServiceLocator.instance().getService("${serviceName.toLowerCase()}Service", ${serviceName}Service.class);
        String strategyName = ${serviceName.toLowerCase()}Service.getStrategyName();

        EngineLocator.instance().initEngine(strategyName);

        EngineLocator.instance().getEngine(strategyName).deployInitModules();

        EngineLocator.instance().getEngine(strategyName).setInternalClock(true);

        EngineLocator.instance().getEngine(strategyName).deployRunModules();
    }
}
