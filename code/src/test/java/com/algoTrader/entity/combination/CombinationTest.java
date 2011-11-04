package com.algoTrader.entity.combination;

import org.junit.Test;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.CombinationType;
import com.algoTrader.service.CombinationService;

public class CombinationTest {

    @Test
    public void test() {


        CombinationService combinationService = ServiceLocator.serverInstance().getCombinationService();

        Combination combination = combinationService.createCombination("EASTWEST", CombinationType.RATIO_SPREAD, 3);

        combinationService.addAllocation(combination.getId(), 3, 3);
        combinationService.addAllocation(combination.getId(), 4, 2);

        combinationService.addAllocation(combination.getId(), 3, 4);

        combinationService.removeAllocation(combination.getId(), 3);

        combinationService.addAllocation(combination.getId(), 3, 5);

        combinationService.deleteCombination("EASTWEST", 3);

        combination = combinationService.createCombination("EASTWEST", CombinationType.RATIO_SPREAD, 4);

    }
}
