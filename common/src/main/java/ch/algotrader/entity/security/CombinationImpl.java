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
package ch.algotrader.entity.security;

import java.util.Objects;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.collection.LongMap;
import ch.algotrader.util.metric.MetricsUtil;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CombinationImpl extends Combination {

    private static final long serialVersionUID = -3967940153149799380L;

    @Override
    public LongMap<Security> getQuantityMap() {

        LongMap<Security> qtyMap = new LongMap<Security>();
        for (Component component : getComponentsInitialized()) {
            qtyMap.increment(component.getSecurityInitialized(), component.getQuantity());
        }

        return qtyMap;
    }

    @Override
    public Component getComponentBySecurity(final Security security) {

        // find the component to the specified security
        return CollectionUtils.find(getComponents(), new Predicate<Component>() {
            @Override
            public boolean evaluate(Component component) {
                return security.equals(component.getSecurity());
            }
        });
    }

    @Override
    public long getComponentQuantity(final Security security) {

        Component component = getComponentBySecurity(security);

        if (component == null) {
            throw new IllegalArgumentException("no component exists for the defined master security");
        } else {
            return component.getQuantity();
        }
    }

    @Override
    public Direction getComponentDirection(final Security security) {

        long qty = getComponentQuantity(security);

        if (qty < 0) {
            return Direction.SHORT;
        } else if (qty > 0) {
            return Direction.LONG;
        } else {
            return Direction.FLAT;
        }
    }

    @Override
    public long getComponentTotalQuantity() {

        long quantity = 0;
        for (Component component : getComponentsInitialized()) {
            quantity += component.getQuantity();
        }
        return quantity;
    }

    @Override
    public int getComponentCount() {
        return getComponents().size();
    }


    @Override
    public String toString() {

        return StringUtils.join(CollectionUtils.collect(getComponentsInitialized(), new Transformer<Component, String>() {
            @Override
            public String transform(Component component) {
                return component.getQuantity() + " " + component.getSecurity();
            }
        }), " + ");
    }

    @Override
    public void initialize() {

        if (!isInitialized()) {

            // initialize components
            long beforeComponents = System.nanoTime();

            getComponentsInitialized();

            MetricsUtil.accountEnd("Combination.components", beforeComponents);

            super.initialize();

        }
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }
        if (obj instanceof Combination) {
            Combination that = (Combination) obj;
            return Objects.equals(this.getUuid(), that.getUuid());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {

        int hash = 17;
        hash = hash * 37 + Objects.hashCode(this.getUuid());
        if (this.getUuid() != null) {
            hash =  hash * 37 + this.getUuid().hashCode();
        }
        return hash;
    }
}
