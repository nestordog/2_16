package com.algoTrader.entity.combination;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.algoTrader.ServiceLocator;
import com.algoTrader.enumeration.CombinationType;
import com.algoTrader.service.CombinationService;
import com.algoTrader.service.LookupService;

public class CombinationTest {

    @Test
    public void test() {

        String strategyName = "EASTWEST";
        int masterSecurityId = 3;
        int secondarySecurityId = 4;

        CombinationService combinationService = ServiceLocator.serverInstance().getCombinationService();
        LookupService lookupService = ServiceLocator.serverInstance().getLookupService();

        Combination combination = combinationService.createCombination(strategyName, CombinationType.RATIO_SPREAD, masterSecurityId);

        combinationService.addAllocation(combination.getId(), masterSecurityId, 1);
        combinationService.addAllocation(combination.getId(), masterSecurityId, 1);
        combinationService.addAllocation(combination.getId(), secondarySecurityId, 3);

        combinationService.removeAllocation(combination.getId(), masterSecurityId);

        combinationService.addAllocation(combination.getId(), masterSecurityId, 5);

        assertNotNull(lookupService.getCombinationByStrategyAndMasterSecurity(strategyName, 3));
        assertNotNull(lookupService.getCombinationsByStrategy(strategyName));

        assertEquals(lookupService.getCombinationsByAnySecurity(strategyName, masterSecurityId).size(), 1);
        assertEquals(lookupService.getCombinationsByMasterSecurity(masterSecurityId).size(), 1);

        combinationService.deleteCombination(strategyName, masterSecurityId);
    }
}
