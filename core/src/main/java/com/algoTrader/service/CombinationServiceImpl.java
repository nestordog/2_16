package com.algoTrader.service;

import java.util.List;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.security.Combination;
import com.algoTrader.entity.security.Component;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.CombinationType;
import com.algoTrader.util.HibernateUtil;
import com.algoTrader.util.MyLogger;

public class CombinationServiceImpl extends CombinationServiceBase {

    private static Logger logger = MyLogger.getLogger(CombinationServiceImpl.class.getName());

    @Override
    protected Combination handleCreateCombination(CombinationType type) throws Exception {

        // create the combination
        Combination combination = Combination.Factory.newInstance();
        combination.setType(type);

        // save to DB
        getCombinationDao().create(combination);

        logger.debug("created combination " + combination);

        return combination;
    }

    @Override
    protected void handleDeleteCombination(int combinationId) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            logger.warn("combination does not exist: " + combinationId);

        } else {

            // remove the combination and all associated components
            getCombinationDao().remove(combination);

            logger.debug("deleted combination " + combination);
        }
    }

    @Override
    protected void handleAddComponent(int combinationId, final int securityId, long quantity) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

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

            // adjust the quantity
            component.setQuantity(component.getQuantity() + quantity);
            getComponentDao().update(component);

        } else {

            // create a new component
            component = Component.Factory.newInstance();
            component.setSecurity(security);
            component.setQuantity(quantity);
            component.setParentSecurity(combination);
            getComponentDao().create(component);

            // update the combination
            combination.getComponents().add(component);
            getCombinationDao().create(combination);
        }

        logger.debug("added component " + quantity + " of " + component + " to combination " + combination);
    }

    @Override
    protected void handleSetComponentQuantity(int combinationId, final int securityId, long quantity) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

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

            // set the quantity
            component.setQuantity(quantity);
            getComponentDao().update(component);

        } else {

            // create a new component
            component = Component.Factory.newInstance();
            component.setSecurity(security);
            component.setQuantity(quantity);
            component.setParentSecurity(combination);
            getComponentDao().create(component);

            // update the combination
            combination.getComponents().add(component);
            getCombinationDao().create(combination);
        }

        logger.debug("set component " + quantity + " of " + component + " to combination " + combination);
    }

    @Override
    protected void handleRemoveComponent(int combinationId, final int securityId) {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

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
            getCombinationDao().update(combination);

            // delete the component
            getComponentDao().remove(component);

        } else {

            throw new IllegalArgumentException("component on securityId " + securityId + " does not exist");
        }

        logger.debug("removed component " + component + " from combination " + combination);
    }

    @Override
    protected void handleCloseCombination(int combinationId, String strategyName) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

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

        // close all positions based on the combination
        Position position = getPositionDao().findBySecurityAndStrategy(combinationId, strategyName);
        getPositionService().deletePosition(position.getId(), true);

        // delete the combination
        deleteCombination(combination.getId());
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleDeleteCombinationsWithZeroQty(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        List<Security> combinations = getSecurityDao().findSubscribedByStrategyAndComponentClassWithZeroQty(strategyName, discriminator);

        getSecurityDao().remove(combinations);

        if (combinations.size() > 0) {
            logger.debug("deleted zero quantity combinations: " + combinations);
        }

        // TODO remove combinations from COMBINATION_TICK_WINDOW
    }
}
