package com.algoTrader.entity.combination;

import org.junit.Test;

import com.algoTrader.ServiceLocator;
import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.CombinationType;
import com.algoTrader.service.CombinationService;

public class CombinationTest {

    @Test
    public void test() {

        Security security1 = ServiceLocator.serverInstance().getLookupService().getSecurity(3);
        Security security2 = ServiceLocator.serverInstance().getLookupService().getSecurity(4);

        CombinationService combinationService = ServiceLocator.serverInstance().getCombinationService();

        Combination combination = combinationService.createCombination("EASTWEST", CombinationType.RATIO_SPREAD, null);

        combinationService.addAllocation(combination.getId(), security1, 3);
        combinationService.addAllocation(combination.getId(), security2, 2);

        combinationService.addAllocation(combination.getId(), security1, 4);

        combinationService.removeAllocation(combination.getId(), security1);

        combinationService.addAllocation(combination.getId(), security1, 5);

        combinationService.deleteCombination(combination.getId());
    }
}
