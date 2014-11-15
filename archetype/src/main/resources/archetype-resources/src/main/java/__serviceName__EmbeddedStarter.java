package ${package};

import ch.algotrader.ServiceLocator;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.starter.BaseStarter;

/**
 * This class starts the strategy in Live Trading Mode in a single VM
 */
public class ${serviceName}EmbeddedStarter extends BaseStarter  {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.SINGLE_BEAN_REFERENCE_LOCATION);

        startBase();

        ${serviceName}Service ${serviceName.toLowerCase()}Service = ServiceLocator.instance().getService("${serviceName.toLowerCase()}Service", ${serviceName}Service.class);
        String strategyName = ${serviceName.toLowerCase()}Service.getStrategyName();

        EngineLocator.instance().initEngine(strategyName);

        EngineLocator.instance().getEngine(strategyName).deployInitModules();

        EngineLocator.instance().getEngine(strategyName).setInternalClock(true);

        EngineLocator.instance().getEngine(strategyName).deployRunModules();
    }
}
