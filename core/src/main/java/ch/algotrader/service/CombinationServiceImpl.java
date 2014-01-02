/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import java.util.Collection;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;

import ch.algotrader.entity.Position;
import ch.algotrader.entity.Subscription;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Component;
import ch.algotrader.entity.security.Security;
import ch.algotrader.entity.security.SecurityFamily;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.esper.EngineLocator;
import ch.algotrader.util.HibernateUtil;
import ch.algotrader.util.MyLogger;
import ch.algotrader.vo.InsertComponentEventVO;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CombinationServiceImpl extends CombinationServiceBase {

    private static final long serialVersionUID = -2720603696641382966L;

    private static Logger logger = MyLogger.getLogger(CombinationServiceImpl.class.getName());

    private @Value("${simulation}") boolean simulation;

    @Override
    protected void handleInit() throws Exception {

        if (!this.simulation) {
            for (Combination combination : getCombinationDao().loadAll()) {
                insertIntoComponentWindow(combination);
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
        combination.setSecurityFamily(securityFamily);

        // save to DB
        getCombinationDao().create(combination);

        // reverse-associate security family (after combination has received an id)
        securityFamily.getSecurities().add(combination);

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

            // remove the component from the ComponentWindow
            removeFromComponentWindow(component);

            // update the ComponentWindow
            insertIntoComponentWindow(combination);

        } else {

            throw new IllegalArgumentException("component on securityId " + securityId + " does not exist");
        }

        logger.debug("removed component " + component + " from combination " + combinationString);

        return combination;
    }

    @Override
    protected void handleCloseCombination(int combinationId, String strategyName) throws Exception {

        Combination combination = getCombinationDao().get(combinationId);

        if (combination == null) {
            logger.warn("combination does not exist: " + combinationId);
            return;
        }

        // reduce all associated positions by the specified amount
        // Note: positions are not closed, because other combinations might relate to them as well
        for (Component component : combination.getComponents()) {

            if (component.getQuantity() != 0) {

                Position position = getPositionDao().findBySecurityAndStrategy(component.getSecurity().getId(), strategyName);

                logger.info("reduce position " + position.getId() + " by " + component.getQuantity());

                getPositionService().reducePosition(position.getId(), component.getQuantity());
            }
        }

        // close non-tradeable position on the combination
        Position position = getPositionDao().findBySecurityAndStrategy(combinationId, strategyName);
        if (position != null) {
            getPositionService().deleteNonTradeablePosition(position.getId(), true);
        }

        // delete the combination
        deleteCombination(combination.getId());
    }

    @Override
    protected Combination handleReduceCombination(int combinationId, String strategyName, double ratio) throws Exception {

        if (ratio >= 1.0) {
            closeCombination(combinationId, strategyName);
            return null;
        } else if (ratio < 0) {
            throw new IllegalArgumentException("ratio cannot be smaller than zero");
        } else {

            Combination combination = getCombinationDao().get(combinationId);
            if (combination == null) {
                throw new IllegalArgumentException("combination does not exist: " + combinationId);
            }

            if (ratio != 0) {

                // reduce all associated positions by the specified ratio
                // Note: positions are not closed, because other combinations might relate to them as well
                for (Component component : combination.getComponents()) {

                    long quantity = -Math.round(component.getQuantity() * ratio);
                    long absQuantity = Math.abs(quantity);

                    // adjust the component
                    addOrRemoveComponentQuantity(combinationId, component.getSecurity().getId(), quantity, true);

                    Position position = getPositionDao().findBySecurityAndStrategy(component.getSecurity().getId(), strategyName);

                    logger.info("reduce position " + position.getId() + " of combination " + combination + " by " + absQuantity);

                    // reduce the position
                    getPositionService().reducePosition(position.getId(), absQuantity);
                }
            }

            return combination;
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected void handleDeleteCombinationsWithZeroQty(String strategyName, Class type) throws Exception {

        int discriminator = HibernateUtil.getDisriminatorValue(getSessionFactory(), type);
        Collection<Combination> combinations = getCombinationDao().findSubscribedByStrategyAndComponentTypeWithZeroQty(strategyName, discriminator);

        if (combinations.size() > 0) {

            for (Combination combination : combinations) {
                deleteCombination(combination.getId());
            }

            logger.debug("deleted zero quantity combinations: " + combinations);
        }
    }

    @Override
    protected void handleResetComponentWindow() throws Exception {

        // emtpy the entire component window
        removeFromComponentWindow(null);

        // reset the component window
        for (Combination combination : getCombinationDao().loadAll()) {
            insertIntoComponentWindow(combination);
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
                if (quantity == 0) {
                    return combination;
                } else {
                    component.setQuantity(component.getQuantity() + quantity);
                }
            } else {
                if (component.getQuantity() == quantity) {
                    return combination;
                } else {
                    component.setQuantity(quantity);
                }
            }

        } else {

            // create a new component
            component = Component.Factory.newInstance();
            component.setSecurity(security);
            component.setQuantity(quantity);

            // associate combination
            component.setCombination(combination);

            getComponentDao().create(component);

            // reverse associate combination (after component has received an id)
            combination.getComponents().add(component);
        }

        // update the ComponentWindow
        insertIntoComponentWindow(combination);

        if (add) {
            logger.debug("added component quantity " + quantity + " of " + component + " to combination " + combinationString);
        } else {
            logger.debug("set component quantity " + quantity + " of " + component + " to combination " + combinationString);
        }

        return combination;
    }

    /**
     * send a RemoveComponentEvent to remove one or all components from the component window
     */
    private void removeFromComponentWindow(Component component) {

        // if a component is specified remove it, otherwise empty the entire window
        if (EngineLocator.instance().hasBaseEngine()) {
            if (component != null) {
                EngineLocator.instance().getBaseEngine().executeQuery("delete from ComponentWindow where componentId = " + component.getId());
            } else {
                EngineLocator.instance().getBaseEngine().executeQuery("delete from ComponentWindow");
            }
        }
    }

    /**
     * reset all entries in the ComponentWindow as parentSecurity.componentCount might have changed
     */
    private void insertIntoComponentWindow(Combination combination) {

        for (Component component : combination.getComponents()) {

            InsertComponentEventVO insertComponentEvent = new InsertComponentEventVO();
            insertComponentEvent.setComponentId(component.getId());
            insertComponentEvent.setQuantity(component.getQuantity());
            insertComponentEvent.setSecurityId(component.getSecurity().getId());
            insertComponentEvent.setCombinationId(combination.getId());
            insertComponentEvent.setComponentCount(combination.getComponentCount());

            if (EngineLocator.instance().hasBaseEngine()) {
                EngineLocator.instance().getBaseEngine().sendEvent(insertComponentEvent);
            }
        }
    }
}
