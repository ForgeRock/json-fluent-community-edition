/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]".
 *
 * Copyright © 2011 ForgeRock AS. All rights reserved.
 */

package org.forgerock.json.fluent;

// Java Standard Edition
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Exposes a range of integer values as a set. Used to expose a set of values without
 * requiring the allocation of storage for all values.
 *
 * @author Paul C. Bryan
 */
class RangeSet extends AbstractSet<Integer> implements Set<Integer>, Cloneable, Serializable {

    static final long serialVersionUID = 1L;

    /** The start of the range, inclusive. */
    private int start;

    /** The end of the range, inclusive. */
    private int end;

    /**
     * Constructs a range set for the specified range.
     *
     * @param start the start of the range, inclusive.
     * @param end the end of the range, inclusive.
     */
    public RangeSet(int start, int end) {
        this.start = start;
        this.end = end;
        if (start > end) {
            throw new IllegalArgumentException("start must be <= end");
        }
    }

    /**
     * Returns an iterator over the elements in this set.
     */
    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<Integer>() {
            int cursor = start;
            public boolean hasNext() {
                return cursor <= end;
            }
            public Integer next() {
                return Integer.valueOf(cursor++);
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * Returns the number of elements in this set.
     */
    @Override
    public int size() {
        return end - start + 1;
    }

    /**
     * Returns {@code false} unconditionally. Range sets always have at least one element.
     */
    @Override
    public boolean isEmpty() {
        return false;
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     */
    @Override
    public boolean contains(Object o) {
        if (o != null && o instanceof Integer) {
            int n = (((Integer)o).intValue());
            return (n >= start && n <= end);
        }
        return false;
    }

    /**
     * Unconditionally throws {@link UnsupportedOperationException}, as range sets are
     * immutable.
     */
    @Override
    public boolean add(Integer e) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unconditionally throws {@link UnsupportedOperationException}, as range sets are
     * immutable.
     */
    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    /**
     * Unconditionally throws {@link UnsupportedOperationException}, as range sets are
     * immutable.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
}