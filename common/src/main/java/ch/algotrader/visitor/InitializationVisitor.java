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
package ch.algotrader.visitor;

import ch.algotrader.entity.Initializer;
import ch.algotrader.entity.security.Combination;
import ch.algotrader.entity.security.Security;

/**
 * An EntityVistor used to initialize specific hibernate relations on a per-entity-class basis
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class InitializationVisitor extends PolymorphicEntityVisitor<Void, Initializer> {

    public static final InitializationVisitor INSTANCE = new InitializationVisitor();

    private InitializationVisitor() {
    }

    @Override
    public Void visitCombination(Combination combination, Initializer initializer) {

        if (!combination.isInitialized()) {
            combination.initializeComponents(initializer);
        }

        return super.visitCombination(combination, initializer);
    }

    @Override
    public Void visitSecurity(Security security, Initializer initializer) {

        if (!security.isInitialized()) {

            // initialize subscriptions before positions because the lazy loaded (= Proxy) Strategy
            // so subscriptions would also get the Proxy instead of the implementation

            security.initializeSubscriptions(initializer);
            security.initializePositions(initializer);
            security.initializeUnderlying(initializer);
            security.initializeSecurityFamily(initializer);

            security.setInitialized();
        }

        return super.visitSecurity(security, initializer);
    }

}
