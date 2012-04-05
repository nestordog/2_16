package com.algoTrader.entity.security;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang.StringUtils;


public class CombinationImpl extends Combination {

    private static final long serialVersionUID = -3967940153149799380L;

    @Override
    public String toString() {

        if (getComponents().size() > 0) {
            return StringUtils.join(CollectionUtils.collect(getComponents(), new Transformer<Component, String>() {
                @Override
                public String transform(Component component) {
                    return component.getQuantity() + " " + component.getSecurity();
                }
            }), " / ");
        } else {
            return getType().toString();
        }
    }
}
