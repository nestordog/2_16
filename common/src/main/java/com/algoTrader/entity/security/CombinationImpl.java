package com.algoTrader.entity.security;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;

import com.algoTrader.util.collection.LongMap;

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
    public String toString() {

        return (getSymbol() != null ? getSymbol() + " " : "") + StringUtils.join(CollectionUtils.collect(getComponentsInitialized(), new Transformer<Component, String>() {
            @Override
            public String transform(Component component) {
                return component.getQuantity() + " " + component.getSecurity();
            }
        }), " / ");
    }
}
