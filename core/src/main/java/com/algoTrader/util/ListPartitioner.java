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
package com.algoTrader.util;

import java.util.AbstractList;
import java.util.List;

/**
 * Provides static methods for List Partitioning.
 *
 * @author <a href="mailto:andyflury@gmail.com">Andy Flury</a>
 *
 * @version $Revision$ $Date$
 */
public class ListPartitioner {

    /**
      * Returns consecutive {@linkplain List#subList(int, int) sublists} of a list,
      * each of the same size (the final list may be smaller). For example,
      * partitioning a list containing {@code [a, b, c, d, e]} with a partition
      * size of 3 yields {@code [[a, b, c], [d, e]]} -- an outer list containing
      * two inner lists of three and two elements, all in the original order.
      *
      * <p>The outer list is unmodifiable, but reflects the latest state of the
      * source list. The inner lists are sublist views of the original list,
      * produced on demand using {@link List#subList(int, int)}, and are subject
      * to all the usual caveats about modification as explained in that API.
      *
      * @param list the list to return consecutive sublists of
      * @param size the desired size of each sublist (the last may be smaller)
      * @return a list of consecutive sublists
      * @throws IllegalArgumentException if {@code partitionSize} is nonpositive
      *
      */
    public static <T> List<List<T>> partition(List<T> list, int size) {

        if (list == null) {
            throw new NullPointerException("'list' must not be null");
        }
        if (!(size > 0)) {
            throw new IllegalArgumentException("'size' must be greater than 0");
        }

        return new Partition<T>(list, size);
    }

    private static class Partition<T> extends AbstractList<List<T>> {

        final List<T> list;
        final int size;

        Partition(List<T> list, int size) {
            this.list = list;
            this.size = size;
        }

        @Override
        public List<T> get(int index) {
            int listSize = size();
            if (listSize < 0) {
                throw new IllegalArgumentException("negative size: " + listSize);
            }
            if (index < 0) {
                throw new IndexOutOfBoundsException("index " + index + " must not be negative");
            }
            if (index >= listSize) {
                throw new IndexOutOfBoundsException("index " + index + " must be less than size " + listSize);
            }
            int start = index * this.size;
            int end = Math.min(start + this.size, this.list.size());
            return this.list.subList(start, end);
        }

        @Override
        public int size() {
            return (this.list.size() + this.size - 1) / this.size;
        }

        @Override
        public boolean isEmpty() {
            return this.list.isEmpty();
        }
    }

}
