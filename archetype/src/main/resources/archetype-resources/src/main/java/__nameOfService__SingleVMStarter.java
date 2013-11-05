package ${package};

import ch.algotrader.starter.BaseStarter;
import ch.algotrader.esper.EsperManager;
import ch.algotrader.starter.BaseStarter;

/**
 * This class starts the strategy in Live Trading Mode in a single VM
 *
 */
public class ${nameOfService}SingleVMStarter extends BaseStarter  {

    public static void main(String[] args) throws Exception {

        ServiceLocator.instance().init(ServiceLocator.SINGLE_BEAN_REFERENCE_LOCATION);

        startBase();

        ${nameOfService}Service ${artifactId}Service = ServiceLocator.instance().getService("${artifactId}Service", ${nameOfService}Service.class);
        String strategyName = null;

        EsperManager.initServiceProvider(strategyName);

        EsperManager.deployInitModules(strategyName);

        // switch to internalClock
        EsperManager.setInternalClock(strategyName, true);

        //activate the rest of the rules
        EsperManager.deployRunModules(strategyName);
    }
}
