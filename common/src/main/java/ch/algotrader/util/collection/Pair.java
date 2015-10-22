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
package ch.algotrader.util.collection;

import java.io.Serializable;

/**
 * General-purpose pair of values of any type. The pair only equals another pair if
 * the objects that form the pair equal, i.e. first pair first object equals (.equals) the second pair first object,
 * and the first pair second object equals the second pair second object.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 */
public final class Pair<First, Second> implements Serializable {
    private First first;
    private Second second;
    private static final long serialVersionUID = -4168417618011472714L;

    /**
     * Construct pair of values.
     * @param first is the first value
     * @param second is the second value
     */
    public Pair(final First first, final Second second) {
        this.first = first;
        this.second = second;
    }

    /**
     * Returns first value within pair.
     * @return first value within pair
     */
    public First getFirst() {
        return this.first;
    }

    /**
     * Returns second value within pair.
     * @return second value within pair
     */
    public Second getSecond() {
        return this.second;
    }

    /**
     * Set the first value of the pair to a new value.
     * @param first value to be set
     */
    public void setFirst(First first) {
        this.first = first;
    }

    /**
     * Set the second value of the pair to a new value.
     * @param second value to be set
     */
    public void setSecond(Second second) {
        this.second = second;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (!(obj instanceof Pair)) {
            return false;
        }

        Pair<?, ?> other = (Pair<?, ?>) obj;

        return (this.first == null ? other.first == null : this.first.equals(other.first)) && (this.second == null ? other.second == null : this.second.equals(other.second));
    }

    @Override
    public int hashCode() {
        return (this.first == null ? 0 : this.first.hashCode()) ^ (this.second == null ? 0 : this.second.hashCode());
    }

    @Override
    public String toString() {
        return "Pair [" + this.first + ':' + this.second + ']';
    }
}
