package com.algoTrader.entity.combination;

import org.apache.commons.collections15.CollectionUtils;
import org.apache.commons.collections15.Predicate;

import com.algoTrader.entity.security.Security;
import com.algoTrader.enumeration.Direction;

public class CombinationImpl extends Combination {

    private static final long serialVersionUID = -3967940153149799380L;

    @Override
    public long getAllocationQuantity(final Security security) {

        // find the allocation to the specified security
        Allocation allocation = CollectionUtils.find(getAllocations(), new Predicate<Allocation>() {
            @Override
            public boolean evaluate(Allocation allocation) {
                return security.equals(allocation.getSecurity());
            }
        });

        if (allocation == null) {
            throw new IllegalArgumentException("no allocation exists for the defined master security");
        } else {
            return allocation.getQuantity();
        }
    }

    @Override
    public long getMasterQuantity() {

        return getAllocationQuantity(getMaster());
    }

    @Override
    public Direction getMasterDirection() {

        if (getMasterQuantity() < 0) {
            return Direction.SHORT;
        } else if (getMasterQuantity() > 0) {
            return Direction.LONG;
        } else {
            return Direction.FLAT;
        }
    }

    @Override
    public String toString() {

        return getStrategy().getName() + " " + getMaster().getSymbol();
    }
}
