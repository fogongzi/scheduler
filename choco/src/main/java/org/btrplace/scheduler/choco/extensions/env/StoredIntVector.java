/*
 * Copyright (c) 2016 University Nice Sophia Antipolis
 *
 * This file is part of btrplace.
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.btrplace.scheduler.choco.extensions.env;

import org.btrplace.scheduler.choco.extensions.env.trail.StoredIntVectorTrail;
import org.chocosolver.memory.IStateIntVector;

/**
 * @author Fabien Hermenier
 */
public class StoredIntVector extends IStateIntVector {
    /**
     * Contains time stamps for all entries (the world index of the last update for each entry)
     */

    public int[] worldStamps;

    protected final StoredIntVectorTrail myTrail;

    /**
     * Constructs a stored search vector with an initial size, and initial values.
     *
     * @param env          The current environment.
     * @param initialSize  The initial size.
     * @param initialValue The initial common value.
     */

    public StoredIntVector(ChunkedTrailing env, int initialSize, int initialValue) {
        super(env, initialSize, initialValue);
        int initialCapacity = Math.max(MIN_CAPACITY, initialSize);
        int w = env.getWorldIndex();

        this.worldStamps = new int[initialCapacity];
        for (int i = 0; i < initialSize; i++) {
            this.worldStamps[i] = w;
        }
        this.myTrail = env.getIntVectorTrail();
    }


    public StoredIntVector(ChunkedTrailing env, int[] entries) {
        super(env, entries);
        int initialCapacity = Math.max(MIN_CAPACITY, entries.length);
        int w = env.getWorldIndex();
        int initialSize = entries.length;

        this.worldStamps = new int[initialCapacity];
        for (int i = 0; i < initialSize; i++) {
            this.worldStamps[i] = w;
        }
        this.myTrail = env.getIntVectorTrail();
    }


    /**
     * Checks if the capacity is great enough, else the capacity
     * is extended.
     *
     * @param minCapacity the necessary capacity.
     */

    private void ensureCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int[] oldData = elementData;
            int[] oldStamps = worldStamps;
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            elementData = new int[newCapacity];
            worldStamps = new int[newCapacity];
            System.arraycopy(oldData, 0, elementData, 0, size.get());
            System.arraycopy(oldStamps, 0, worldStamps, 0, size.get());
        }
    }


    /**
     * Adds a new search at the end of the vector.
     *
     * @param i The search to add.
     */
    @Override
    public void add(int i) {
        int newsize = size.get() + 1;
        ensureCapacity(newsize);
        size.set(newsize);
        elementData[newsize - 1] = i;
        worldStamps[newsize - 1] = environment.getWorldIndex();
    }

    /**
     * Removes an int.
     *
     * @param i The search to remove.
     */
    @Override
    public void remove(int i) {
        System.arraycopy(elementData, i, elementData, i + 1, size.get());
        System.arraycopy(worldStamps, i, worldStamps, i + 1, size.get());
        int newsize = size.get() - 1;
        if (newsize >= 0)
            size.set(newsize);
    }

    /**
     * removes the search at the end of the vector.
     * does nothing when called on an empty vector
     */
    @Override
    public void removeLast() {
        int newsize = size.get() - 1;
        if (newsize >= 0)
            size.set(newsize);
    }


    /**
     * Assigns a new value <code>val</code> to the element <code>index</code>.
     */
    @Override
    public int set(int index, int val) {
        if (rangeCheck(index)) {
            return quickSet(index, val);
        }
        throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
    }

    @Override
    public final int quickSet(int index, int val) {
        assert rangeCheck(index);
        final int oldValue = elementData[index];
        if (val != oldValue) {
            final int oldStamp = this.worldStamps[index];
            if (oldStamp < environment.getWorldIndex()) {
                myTrail.savePreviousState(this, index, oldValue, oldStamp);
                worldStamps[index] = environment.getWorldIndex();
            }
            elementData[index] = val;
        }
        return oldValue;
    }


    /**
     * Sets an element without storing the previous value.
     */
    public int _set(int index, int val, int stamp) {
        assert rangeCheck(index);
        int oldval = elementData[index];
        elementData[index] = val;
        worldStamps[index] = stamp;
        return oldval;
    }


}
