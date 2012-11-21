package com.algoTrader.entity.security;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;


public class CombinationImpl extends Combination {

    private static final long serialVersionUID = -3967940153149799380L;

    @Override
    public String toString() {

        return (getSymbol() != null ? getSymbol() + " " : "") + StringUtils.join(CollectionUtils.collect(getComponentsInitialized(), new Transformer<Component, String>() {
            @Override
            public String transform(Component component) {
                return component.getQuantity() + " " + component.getSecurityInitialized();
            }
        }), " / ");
    }
}
