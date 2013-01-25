package com.algoTrader.util;

import java.util.Collection;

public class CollectionUtil {

    public static <E> E getSingleElement(Collection<E> collection) {

        if (collection.isEmpty()) {
            throw new IllegalStateException("collection is empty");
        } else if (collection.size() > 1) {
            throw new IllegalStateException("collection has more than one elements");
        } else {
            return collection.iterator().next();
        }
    }

    public static <E> E getSingleElementOrNull(Collection<E> collection) {

        if (collection.isEmpty()) {
            return null;
        } else if (collection.size() > 1) {
            throw new IllegalStateException("collection has more than one elements");
        } else {
            return collection.iterator().next();
        }
    }

    public static <E> E getFirstElement(Collection<E> collection) {

        if (collection.isEmpty()) {
            throw new IllegalStateException("collection is empty");
        } else {
            return collection.iterator().next();
        }
    }

    public static <E> E getFirstElementOrNull(Collection<E> collection) {

        if (collection.isEmpty()) {
            return null;
        } else {
            return collection.iterator().next();
        }
    }
}
