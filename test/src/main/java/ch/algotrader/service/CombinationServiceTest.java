/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2014 AlgoTrader GmbH - All rights reserved
 *
 * All information contained herein is, and remains the property of AlgoTrader GmbH.
 * The intellectual and technical concepts contained herein are proprietary to
 * AlgoTrader GmbH. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from AlgoTrader GmbH
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * AlgoTrader GmbH
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.service;

import org.junit.Assert;
import org.junit.Test;

import ch.algotrader.ServiceLocator;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Component;
import ch.algotrader.enumeration.CombinationType;
import ch.algotrader.util.collection.CollectionUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CombinationServiceTest extends LocalServiceTest {

    @Test
    public void test() throws Exception {

        CombinationService combinationService = ServiceLocator.instance().getCombinationService();
        LookupService lookupService = ServiceLocator.instance().getLookupService();

        // create a combination
        int combinationId = combinationService.createCombination(CombinationType.RATIO_SPREAD, 42).getId();

        // load the combination from the db
        Combination combination = (Combination)lookupService.getSecurity(combinationId);
        Assert.assertNotNull(combination);

        // add a component
        combination = combinationService.addComponentQuantity(combinationId, 8, 1);
        Assert.assertEquals(combination.getComponentCount(), 1);

        // add one more of the same security
        combination = combinationService.addComponentQuantity(combinationId, 8, 1);
        Component component = CollectionUtil.getSingleElement(combination.getComponents());
        Assert.assertEquals(combination.getComponentCount(), 1);
        Assert.assertEquals(component.getQuantity(), 2);

        // add another component of a different security
        combination = combinationService.addComponentQuantity(combinationId, 10, 3);
        Assert.assertEquals(combination.getComponentCount(), 2);

        // remove a component
        combination = combinationService.removeComponent(combinationId, 8);
        Assert.assertEquals(combination.getComponentCount(), 1);

        // re-add the component
        combination = combinationService.addComponentQuantity(combinationId, 8, 5);
        Assert.assertEquals(combination.getComponentCount(), 2);

        // delete the combination
        combinationService.deleteCombination(combinationId);
        combination = (Combination)lookupService.getSecurity(combinationId);
        Assert.assertNull(combination);
    }
}
