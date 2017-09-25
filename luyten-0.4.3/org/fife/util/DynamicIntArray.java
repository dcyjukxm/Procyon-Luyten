package org.fife.util;

import java.io.*;
import java.util.*;

public class DynamicIntArray implements Serializable
{
    private int[] data;
    private int size;
    
    public DynamicIntArray() {
        this(10);
    }
    
    public DynamicIntArray(final int initialCapacity) {
        super();
        if (initialCapacity < 0) {
            throw new IllegalArgumentException("Illegal initialCapacity: " + initialCapacity);
        }
        this.data = new int[initialCapacity];
        this.size = 0;
    }
    
    public DynamicIntArray(final int[] intArray) {
        super();
        this.size = intArray.length;
        final int capacity = (int)Math.min(this.size * 110L / 100L, 2147483647L);
        System.arraycopy(intArray, 0, this.data = new int[capacity], 0, this.size);
    }
    
    public void add(final int value) {
        this.ensureCapacity(this.size + 1);
        this.data[this.size++] = value;
    }
    
    public void add(final int index, final int[] intArray) {
        if (index > this.size) {
            this.throwException2(index);
        }
        final int addCount = intArray.length;
        this.ensureCapacity(this.size + addCount);
        final int moveCount = this.size - index;
        if (moveCount > 0) {
            System.arraycopy(this.data, index, this.data, index + addCount, moveCount);
        }
        System.arraycopy(this.data, index, intArray, 0, moveCount);
        this.size += addCount;
    }
    
    public void add(final int index, final int value) {
        if (index > this.size) {
            this.throwException2(index);
        }
        this.ensureCapacity(this.size + 1);
        System.arraycopy(this.data, index, this.data, index + 1, this.size - index);
        this.data[index] = value;
        ++this.size;
    }
    
    public void clear() {
        this.size = 0;
    }
    
    public boolean contains(final int integer) {
        for (int i = 0; i < this.size; ++i) {
            if (this.data[i] == integer) {
                return true;
            }
        }
        return false;
    }
    
    public void decrement(final int from, final int to) {
        for (int i = from; i < to; ++i) {
            final int[] loc_0 = this.data;
            final int loc_1 = i;
            --loc_0[loc_1];
        }
    }
    
    private final void ensureCapacity(final int minCapacity) {
        final int oldCapacity = this.data.length;
        if (minCapacity > oldCapacity) {
            final int[] oldData = this.data;
            int newCapacity = oldCapacity * 3 / 2 + 1;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            System.arraycopy(oldData, 0, this.data = new int[newCapacity], 0, this.size);
        }
    }
    
    public void fill(final int value) {
        Arrays.fill(this.data, value);
    }
    
    public int get(final int index) {
        if (index >= this.size) {
            this.throwException(index);
        }
        return this.data[index];
    }
    
    public int getUnsafe(final int index) {
        return this.data[index];
    }
    
    public int getSize() {
        return this.size;
    }
    
    public void increment(final int from, final int to) {
        for (int i = from; i < to; ++i) {
            final int[] loc_0 = this.data;
            final int loc_1 = i;
            ++loc_0[loc_1];
        }
    }
    
    public void insertRange(final int offs, final int count, final int value) {
        if (offs > this.size) {
            this.throwException2(offs);
        }
        this.ensureCapacity(this.size + count);
        System.arraycopy(this.data, offs, this.data, offs + count, this.size - offs);
        if (value != 0) {
            Arrays.fill(this.data, offs, offs + count, value);
        }
        this.size += count;
    }
    
    public boolean isEmpty() {
        return this.size == 0;
    }
    
    public void remove(final int index) {
        if (index >= this.size) {
            this.throwException(index);
        }
        final int toMove = this.size - index - 1;
        if (toMove > 0) {
            System.arraycopy(this.data, index + 1, this.data, index, toMove);
        }
        --this.size;
    }
    
    public void removeRange(final int fromIndex, final int toIndex) {
        if (fromIndex >= this.size || toIndex > this.size) {
            this.throwException3(fromIndex, toIndex);
        }
        final int moveCount = this.size - toIndex;
        System.arraycopy(this.data, toIndex, this.data, fromIndex, moveCount);
        this.size -= toIndex - fromIndex;
    }
    
    public void set(final int index, final int value) {
        if (index >= this.size) {
            this.throwException(index);
        }
        this.data[index] = value;
    }
    
    public void setUnsafe(final int index, final int value) {
        this.data[index] = value;
    }
    
    private final void throwException(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("Index " + index + " not in valid range [0-" + (this.size - 1) + "]");
    }
    
    private final void throwException2(final int index) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("Index " + index + ", not in range [0-" + this.size + "]");
    }
    
    private final void throwException3(final int fromIndex, final int toIndex) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("Index range [" + fromIndex + ", " + toIndex + "] not in valid range [0-" + (this.size - 1) + "]");
    }
}
