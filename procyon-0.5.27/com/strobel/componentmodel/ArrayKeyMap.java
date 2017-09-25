package com.strobel.componentmodel;

import com.strobel.annotations.*;
import com.strobel.core.*;
import java.util.*;

final class ArrayKeyMap implements FrugalKeyMap
{
    static final int ARRAY_THRESHOLD = 8;
    private final int[] _keyIndexes;
    private final Object[] _values;
    
    ArrayKeyMap(final int[] keyIndexes, final Object[] values) {
        super();
        this._keyIndexes = keyIndexes;
        this._values = values;
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");
        final int keyIndex = key.hashCode();
        final int[] oldKeys = this._keyIndexes;
        final int oldLength = oldKeys.length;
        int i = 0;
        while (i < oldLength) {
            final int oldKey = oldKeys[i];
            if (oldKey == keyIndex) {
                final Object oldValue = this._values[i];
                if (oldValue == value) {
                    return this;
                }
                final Object[] newValues = Arrays.copyOf(this._values, oldLength);
                newValues[i] = value;
                return new ArrayKeyMap(oldKeys, newValues);
            }
            else {
                ++i;
            }
        }
        final int[] newKeys = Arrays.copyOf(oldKeys, oldLength + 1);
        final Object[] newValues = Arrays.copyOf(this._values, oldLength + 1);
        newValues[oldLength] = value;
        newKeys[oldLength] = keyIndex;
        return new ArrayKeyMap(newKeys, newValues);
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        final int keyIndex = key.hashCode();
        final int[] oldKeys = this._keyIndexes;
        final int oldLength = oldKeys.length;
        int i = 0;
        while (i < oldLength) {
            final int oldKey = oldKeys[i];
            if (keyIndex == oldKey) {
                final int newLength = oldLength - 1;
                final Object[] oldValues = this._values;
                if (newLength != 2) {
                    final int[] newKeys = new int[newLength];
                    final Object[] newValues = new Object[newLength];
                    System.arraycopy(oldKeys, 0, newKeys, 0, i);
                    System.arraycopy(oldKeys, i + 1, newKeys, i, oldLength - i - 1);
                    System.arraycopy(oldValues, 0, newValues, 0, i);
                    System.arraycopy(oldValues, i + 1, newValues, i, oldLength - i - 1);
                    return new ArrayKeyMap(newKeys, newValues);
                }
                switch (i) {
                    case 0: {
                        return new PairKeyMap(1, oldValues[1], oldKeys[2], oldValues[2]);
                    }
                    case 1: {
                        return new PairKeyMap(0, oldValues[0], oldKeys[2], oldValues[2]);
                    }
                    default: {
                        return new PairKeyMap(0, oldValues[0], oldKeys[1], oldValues[1]);
                    }
                }
            }
            else {
                ++i;
            }
        }
        return this;
    }
    
    @Override
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        final int keyIndex = key.hashCode();
        for (int i = 0; i < this._keyIndexes.length; ++i) {
            if (this._keyIndexes[i] == keyIndex) {
                return (V)this._values[i];
            }
        }
        return null;
    }
    
    @Override
    public final boolean isEmpty() {
        return false;
    }
}
