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

    protected Combination handleCreateCombination(String strategyName, CombinationType type, Security masterSecurity)
            throws Exception {

        Strategy strategy = getStrategyDao().findByName(strategyName);

        // create the combination
        Combination combination = Combination.Factory.newInstance();
        combination.setType(type);
        combination.setStrategy(strategy);
        combination.setMaster(masterSecurity);

        // save to DB
        getCombinationDao().create(combination);

        return combination;
    }

    protected void handleAddAllocation(int combinationId, final Security security, long quantity) throws Exception {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
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

    protected void handleRemoveAllocation(int combinationId, final Security security) {

        Combination combination = getCombinationDao().load(combinationId);

        if (combination == null) {
            throw new IllegalArgumentException("combination does not exist: " + combinationId);
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
