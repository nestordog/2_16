/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2015 AlgoTrader GmbH - All rights reserved
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
 * Aeschstrasse 6
 * 8834 Schindellegi
 ***********************************************************************************/
package ch.algotrader.service;

import ch.algotrader.entity.security.Combination;
import ch.algotrader.enumeration.CombinationType;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public interface CombinationService {

    /**
     * Creates a Combination of the specified {@link CombinationType} and assigns it to the
     * specified {@link ch.algotrader.entity.security.SecurityFamily SecurityFamily}
     */
    public Combination createCombination(CombinationType type, long securityFamilyId);

    /**
     * Creates a Combination of the specified {@link CombinationType}, assigns it to the specified
     * {@link ch.algotrader.entity.security.SecurityFamily SecurityFamily} and assigns an underlying
     * Security defined by the {@code underlyingId}.
     */
    public Combination createCombination(CombinationType type, long securityFamilyId, long underlyingId);

    /**
     * Deletes the specified Combination.
     */
    public void deleteCombination(long combinationId);

    /**
     * Adds the specified {@code quantity} to the Component defined by {@code securityId} of the
     * Combination defined by {code combinationId}. If the Component does not exist yet, it will be
     * created.
     */
    public Combination addComponentQuantity(long combinationId, long securityId, long quantity);

    /**
     * Sets the {@code quantity} of the Component defined by {@code securityId} of the Combination
     * defined by {code combinationId}. the existing {@code quantity} will be ignored. If the
     * Component does not exist yet, it will be created.
     */
    public Combination setComponentQuantity(long combinationId, long securityId, long quantity);

    /**
     * Removes the specified Component defined by {@code securityId} from the Combination defined by
     * {code combinationId}.
     */
    public Combination removeComponent(long combinationId, long securityId);

    /**
     * Updates the Component Window. This method should only be called after manually manipulating
     * components in the DB.
     */
    public void resetComponentWindow();

}
