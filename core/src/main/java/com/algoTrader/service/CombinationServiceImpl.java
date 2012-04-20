package com.algoTrader.service;

import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.StrategyImpl;
import com.algoTrader.entity.Subscription;
import com.algoTrader.entity.security.Combination;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.Security;
import com.algoTrader.entity.security.SecurityFamily;
import com.algoTrader.enumeration.CombinationType;
import com.algoTrader.esper.EsperManager;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;
import com.algoTrader.vo.InsertComponentEventVO;
import com.algoTrader.vo.RemoveComponentEventVO;

public class CombinationServiceImpl extends CombinationServiceBase {

    private static final long serialVersionUID = -2720603696641382966L;

    private static Logger logger = MyLogger.getLogger(CombinationServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;

    @Override
    protected void handleInit() throws Exception {

        if (!this.simulation) {
            for (Combination combination : getCombinationDao().loadAll()) {
                resetComponentWindow(combination);
            }
        }
    }

    @Override
    protected Combination handleCreateCombination(CombinationType type, int securityFamilyId) throws Exception {

        // create the combination
        Combination combination = Combination.Factory.newInstance();
        combination.setType(type);

        // attach the security family
        SecurityFamily securityFamily = getSecurityFamilyDao().get(securityFamilyId);

        // associate the security family
        securityFamily.addSecurities(combination);

        // save to DB
        getCombinationDao().create(combination);

        logger.debug("created combination " + combination);

        return combination;
    }

    @Override
    protected Combination handleCreateCombination(CombinationType type, int securityFamilyId, int underlyingId) throws Exception {

        Security underlying = getSecurityDao().load(underlyingId);
        if (underlying == null) {
            throw new IllegalArgumentException("underlying does not exist: " + underlyingId);
        }

        Combination combination = createCombination(type, securityFamilyId);
        combination.setUnderlying(underlying);

        return combination;
    }

    @Override
    protected void handleDeleteCombination(int combinationId) throws Exception {

        Combination combination = getCombinationDao().get(combinationId);

        if (combination == null) {
            logger.warn("combination does not exist: " + combinationId);

        } else {

            // unsubscribe potential subscribers
            for (Subscription subscription : combination.getSubscriptions()) {
                getMarketDataService().unsubscribe(subscription.getStrategy().getName(), subscription.getSecurity().getId());
            }

            // update the ComponentWindow
            for (Component component : combination.getComponents()) {

                // update the ComponentWindow
                removeFromComponentWindow(component);
            }

            // disassociated the security family
            combination.getSecurityFamily().removeSecurities(combination);

            // remove the combination
            getCombinationDao().remove(combination);

            logger.debug("deleted combination " + combination);
        }
    }

    @Override
    protected Combination handleAddComponentQuantity(int combinationId, final int securityId, long quantity) throws Exception {

        return addOrRemoveComponentQuantity(combinationId, securityId, quantity, true);
    }

    @Override
    protected Combination handleSetComponentQuantity(int combinationId, int securityId, long quantity) throws Exception {

        return addOrRemoveComponentQuantity(combinationId, securityId, quantity, false);
    }


    @Override
    protected Combination handleRemoveComponent(int combinationId, final int securityId) {

        Combination combination = getCombinationDao().get(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        String combinationString = combination.toString();
        final Security security = getSecurityDao().load(securityId);

        if (security == null) {
            throw new IllegalArgumentException("security does not exist: " + securityId);
        }

        // find the component to the specified security
        Component component = CollectionUtils.find(combination.getComponents(), new Predicate<Component>() {
            @Override
            public boolean evaluate(Component component) {
                return security.equals(component.getSecurity());
            }
        });

        if (component != null) {

            // update the combination
            combination.getComponents().remove(component);

            // delete the component
            getComponentDao().remove(component);

            // update the ComponentWindow
            removeFromComponentWindow(component);
            resetComponentWindow(combination);

        } else {

            throw new IllegalArgumentException("component on securityId " + securityId + " does not exist");
        }

        logger.debug("removed component " + component + " from combination " + combinationString);

        return combination;
    }

    private void removeFromComponentWindow(Component component) {

        RemoveComponentEventVO removeComponentEvent = new RemoveComponentEventVO();
        removeComponentEvent.setComponentId(component.getId());
        EsperManager.routeEvent(StrategyImpl.BASE, removeComponentEvent);
    }

    @Override
    protected void handleCloseCombination(int combinationId, String strategyName) throws Exception {

        Combination combination = getCombinationDao().get(combinationId);

        if (combination == null) {
            logger.warn("combination does not exist: " + combinationId);
            return;
        }

        logger.info("close all positions of combination " + combination + " and delete combination");

        // reduce all associated positions by the specified amount
        // Note: positions are not closed, because other combinations might relate to them as well
        for (Component component : combination.getComponents()) {

            Position position = getPositionDao().findBySecurityAndStrategy(component.getSecurity().getId(), strategyName);
            getPositionService().reducePosition(position.getId(), component.getQuantity());
        }

        // close all non-tradeable positions based on the combination
        Position position = getPositionDao().findBySecurityAndStrategy(combinationId, strategyName);
        getPositionService().deleteNonTradeablePosition(position.getId(), true);

        // delete the combination
        deleteCombination(combination.getId());
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleDeleteCombinationsWithZeroQty(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        List<Security> combinations = getSecurityDao().findSubscribedByStrategyAndComponentClassWithZeroQty(strategyName, discriminator);

        if (combinations.size() > 0) {

            for (Security security : combinations) {
                deleteCombination(security.getId());
            }

            logger.debug("deleted zero quantity combinations: " + combinations);
        }
    }

    private Combination addOrRemoveComponentQuantity(int combinationId, final int securityId, long quantity, boolean add) throws Exception {

        Combination combination = getCombinationDao().get(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        String combinationString = combination.toString();
        final Security security = getSecurityDao().load(securityId);

        if (security == null) {
            throw new IllegalArgumentException("security does not exist: " + securityId);
        }

        // find the component to the specified security
        Component component = CollectionUtils.find(combination.getComponents(), new Predicate<Component>() {
            @Override
            public boolean evaluate(Component component) {
                return security.equals(component.getSecurity());
            }
        });

        if (component != null) {

            // add or set the quantity
            if (add) {
                component.setQuantity(component.getQuantity() + quantity);
            } else {
                component.setQuantity(quantity);
            }

        } else {

            // create a new component
            component = Component.Factory.newInstance();
            component.setSecurity(security);
            component.setQuantity(quantity);

            // associate with combination
            combination.addComponents(component);

            getComponentDao().create(component);

        }

        resetComponentWindow(combination);

        if (add) {
            logger.debug("added component quantity " + quantity + " of " + component + " to combination " + combinationString);
        } else {
            logger.debug("set component quantity " + quantity + " of " + component + " to combination " + combinationString);
        }

        return combination;
    }

    /**
     * reset all entries in the ComponentWindow as parent Security has changed due to changes in component qty of one of the components
     */
    private void resetComponentWindow(Combination combination) {

        for (Component component : combination.getComponents()) {

            InsertComponentEventVO insertComponentEvent = new InsertComponentEventVO();
            insertComponentEvent.setComponentId(component.getId());
            insertComponentEvent.setQuantity(component.getQuantity());
            insertComponentEvent.setSecurityId(component.getSecurity().getId());
            insertComponentEvent.setParentSecurity(combination);
            EsperManager.routeEvent(StrategyImpl.BASE, insertComponentEvent);
        }
    }
}
