package com.algoTrader.service;

import java.text.DecimalFormat;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.log4j.Logger;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.combination.Allocation;
import com.algoTrader.entity.combination.Combination;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.CombinationType;
import com.algoTrader.util.MyLogger;

public class CombinationServiceImpl extends CombinationServiceBase {

    private static Logger logger = MyLogger.getLogger(CombinationServiceImpl.class.getName());
    private static DecimalFormat format = new DecimalFormat("#,##0.0000");

    @Override
    protected Combination handleCreateCombination(String strategyName, CombinationType type, int masterSecurityId)
            throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        // create the combination
        Combination combination = Combination.Factory.newInstance();
        combination.setType(type);
        combination.setStrategy(strategy);

        Security masterSecurity = getSecurityDao().load(masterSecurityId);

        if (masterSecurity == null) {
            throw new IllegalArgumentException("security does not exist: " + masterSecurityId);
        }

        combination.setMaster(masterSecurity);

        // save to DB
        getCombinationDao().create(combination);

        logger.debug("created combination " + combination);

        return combination;
    }

    @Override
    protected void handleDeleteCombination(int combinationId) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        // remove the combination and all associated allocations
        getAllocationDao().remove(combination.getAllocations());
        getCombinationDao().remove(combination);

        logger.debug("deleted combination " + combination);
    }

    @Override
    protected void handleDeleteCombination(String strategyName, int masterSecurityId) throws Exception {

        Combination combination = getCombinationDao().findByStrategyAndMasterSecurity(strategyName, masterSecurityId);

        if (combination != null) {

            // remove the combination and all associated allocations
            getAllocationDao().remove(combination.getAllocations());
            getCombinationDao().remove(combination);

            logger.debug("deleted combination " + combination);
        }
    }

    @Override
    protected void handleAddAllocation(int combinationId, final int securityId, long quantity) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        final Security security = getSecurityDao().load(securityId);

        if (security == null) {
            throw new IllegalArgumentException("security does not exist: " + securityId);
        }

        // find the allocation to the specified security
        Allocation allocation = CollectionUtils.find(combination.getAllocations(), new Predicate<Allocation>() {
            @Override
            public boolean evaluate(Allocation allocation) {
                return security.equals(allocation.getSecurity());
            }
        });

        if (allocation != null) {

            // adjust the quantity
            allocation.setQuantity(allocation.getQuantity() + quantity);
            getAllocationDao().update(allocation);

        } else {

            // create a new allocation
            allocation = Allocation.Factory.newInstance();
            allocation.setSecurity(security);
            allocation.setQuantity(quantity);
            allocation.setCombination(combination);
            getAllocationDao().create(allocation);

            // update the combination
            combination.getAllocations().add(allocation);
            getCombinationDao().create(combination);
        }

        logger.debug("added allocation " + quantity + " of " + allocation + " to combination " + combination);
    }

    @Override
    protected void handleSetAllocation(int combinationId, final int securityId, long quantity) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        final Security security = getSecurityDao().load(securityId);

        if (security == null) {
            throw new IllegalArgumentException("security does not exist: " + securityId);
        }

        // find the allocation to the specified security
        Allocation allocation = CollectionUtils.find(combination.getAllocations(), new Predicate<Allocation>() {
            @Override
            public boolean evaluate(Allocation allocation) {
                return security.equals(allocation.getSecurity());
            }
        });

        if (allocation != null) {

            // set the quantity
            allocation.setQuantity(quantity);
            getAllocationDao().update(allocation);

        } else {

            // create a new allocation
            allocation = Allocation.Factory.newInstance();
            allocation.setSecurity(security);
            allocation.setQuantity(quantity);
            allocation.setCombination(combination);
            getAllocationDao().create(allocation);

            // update the combination
            combination.getAllocations().add(allocation);
            getCombinationDao().create(combination);
        }

        logger.debug("set allocation " + allocation + " to combination " + combination);
    }

    @Override
    protected void handleRemoveAllocation(int combinationId, final int securityId) {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        final Security security = getSecurityDao().load(securityId);

        if (security == null) {
            throw new IllegalArgumentException("security does not exist: " + securityId);
        }

        // find the allocation to the specified security
        Allocation allocation = CollectionUtils.find(combination.getAllocations(), new Predicate<Allocation>() {
            @Override
            public boolean evaluate(Allocation allocation) {
                return security.equals(allocation.getSecurity());
            }
        });

        if (allocation != null) {

            // update the combination
            combination.getAllocations().remove(allocation);
            getCombinationDao().update(combination);

            // delete the allocation
            getAllocationDao().remove(allocation);

        } else {

            throw new IllegalArgumentException("allocation on securityId " + securityId + " does not exist");
        }

        logger.debug("removed allocation " + allocation + " from combination " + combination);
    }

    @Override
    protected void handleSetExitValue(int combinationId, double exitValue) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        combination.setExitValue(exitValue);

        getCombinationDao().update(combination);

        logger.info("set exit value " + format.format(exitValue) + " for combination " + combination);
    }

    @Override
    protected void handleCloseCombination(int combinationId) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        logger.info("close all positions of combination " + combination + " and delete combination");

        // reduce all associated positions by the specified amount
        // Note: positions are not closed, because other combinations might relate to them as well
        for (Allocation allocation : combination.getAllocations()) {

            Position position = getPositionDao().findBySecurityAndStrategy(allocation.getSecurity().getId(), combination.getStrategy().getName());
            getPositionService().reducePosition(position.getId(), allocation.getQuantity());
        }


        deleteCombination(combination.getId());
    }
}
