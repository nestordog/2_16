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
package ch.algotrader.entity.security;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

import ch.algotrader.enumeration.Direction;
import ch.algotrader.util.collection.LongMap;

/**
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public class CombinationImpl extends Combination {

    private static final long serialVersionUID = -3967940153149799380L;

    @Override
    public LongMap<Security> getQuantityMap() {

        LongMap<Security> qtyMap = new LongMap<>();
        for (Component component : getComponents()) {
            qtyMap.increment(component.getSecurity(), component.getQuantity());
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
        for (Component component : getComponents()) {
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

        String name = StringUtils.join(CollectionUtils.collect(getComponents(), new Transformer<Component, String>() {
            @Override
            public String transform(Component component) {
                return component.getQuantity() + " " + component.getSecurity();
            }
        }), " + ");
        if (StringUtils.isNotBlank(name)) {
            return name;
        }
        if (StringUtils.isNotBlank(getSymbol())) {
            return getSymbol();
        } else {
            return "EMPTY_COMBINATION";
        }
    }

}
