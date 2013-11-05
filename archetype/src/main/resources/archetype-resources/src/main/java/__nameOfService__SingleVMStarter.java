package ${package};

import ch.algotrader.starter.BaseStarter;
import ch.algotrader.esper.EsperManager;

/**
 * This class starts the strategy in Live Trading Mode in a single VM
 *
 */
public class ${nameOfService}SingleVMStarter extends BaseStarter  {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.SINGLE_BEAN_REFERENCE_LOCATION);

        startBase();

        ${nameOfService}Service ${artifactId}Service = ServiceLocator.instance().getService("${artifactId}Service", ${nameOfService}Service.class);
        String strategyName = ${artifactId}Service.getStrategyName();

        EsperManager.initServiceProvider(strategyName);

        EsperManager.deployInitModules(strategyName);

        EsperManager.setInternalClock(strategyName, true);

        EsperManager.deployRunModules(strategyName);
    }
}
