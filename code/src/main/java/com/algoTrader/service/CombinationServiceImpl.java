package com.algoTrader.service;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import com.algoTrader.entity.Position;
import com.algoTrader.entity.Strategy;
import com.algoTrader.entity.combination.Allocation;
import com.algoTrader.entity.combination.Combination;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.CombinationType;

public class CombinationServiceImpl extends CombinationServiceBase {

    @Override
    protected Combination handleCreateCombination(String strategyName, CombinationType type, int masterSecurityId)
            throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        // create the combination
        Combination combination = Combination.Factory.newInstance();
        combination.setType(type);
        combination.setStrategy(strategy);

        if (masterSecurityId != 0) {
            Security masterSecurity = getSecurityDao().load(masterSecurityId);

            if (masterSecurity == null) {
                throw new IllegalArgumentException("security does not exist: " + masterSecurityId);
            }

            combination.setMaster(masterSecurity);
        }

        // save to DB
        getCombinationDao().create(combination);

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
    }

    @Override
    protected void handleDeleteCombination(String strategyName, int masterSecurityId) throws Exception {

        Combination combination = getCombinationDao().findByStrategyAndMasterSecurity(strategyName, masterSecurityId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist for strategy: " + strategyName + " and masterSecurityId: " + masterSecurityId);
        }

        deleteCombination(combination.getId());
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
        }
    }

    @Override
    protected void handleSetExitValue(int combinationId, double exitValue) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        combination.setExitValue(exitValue);

        getCombinationDao().update(combination);
    }

    @Override
    protected void handleCloseCombination(int combinationId) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
        }

        // TODO create OrderCallbacks to adjust the quantities of the allocations

        for (Allocation allocation : combination.getAllocations()) {

            Position position = getPositionDao().findBySecurityAndStrategy(allocation.getSecurity().getId(), combination.getStrategy().getName());
            getPositionService().reducePosition(position.getId(), allocation.getQuantity());
        }
    }
}
