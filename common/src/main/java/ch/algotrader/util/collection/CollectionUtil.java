/***********************************************************************************
 * AlgoTrader Enterprise Trading Framework
 *
 * Copyright (C) 2013 Flury Trading - All rights reserved
 *
 * All information contained herein is, and remains the property of Flury Trading.
 * The intellectual and technical concepts contained herein are proprietary to
 * Flury Trading. Modification, translation, reverse engineering, decompilation,
 * disassembly or reproduction of this material is strictly forbidden unless prior
 * written permission is obtained from Flury Trading
 *
 * Fur detailed terms and conditions consult the file LICENSE.txt or contact
 *
 * Flury Trading
 * Badenerstrasse 16
 * 8004 Zurich
 ***********************************************************************************/
package ch.algotrader.util.collection;

import java.util.Collection;

/**
 * Provides single/first element lookup methods for Collections.
 *
 * @author <a href="mailto:aflury@algotrader.ch">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class CollectionUtil {

    /**
     * returns the one and only element of a collection.
     * @throws IllegalStateException if the collection is empty or has more than one element.
     */
    public static <E> E getSingleElement(Collection<E> collection) {

        if (collection.isEmpty()) {
            throw new IllegalStateException("collection is empty");
        } else if (collection.size() > 1) {
            throw new IllegalStateException("collection has more than one elements");
        } else {
            return collection.iterator().next();
        }
    }

    /**
     * returns the one and only element of a collection or null if the collection is empty.
     * @throws IllegalStateException if the collection has more than one element.
     */
    public static <E> E getSingleElementOrNull(Collection<E> collection) {

        if (collection.isEmpty()) {
            return null;
        } else if (collection.size() > 1) {
            throw new IllegalStateException("collection has more than one elements");
        } else {
            return collection.iterator().next();
        }
    }

    /**
     * returns the first element of a collection.
     * @throws IllegalStateException if the collection is empty
     */
    public static <E> E getFirstElement(Collection<E> collection) {

        if (collection.isEmpty()) {
            throw new IllegalStateException("collection is empty");
        } else {
            return collection.iterator().next();
        }
    }

    /**
     * returns the first element of a collection or null if the collection is empty.
     */
    public static <E> E getFirstElementOrNull(Collection<E> collection) {

        if (collection.isEmpty()) {
            return null;
        } else {
            return collection.iterator().next();
        }
    }
}
