package com.strobel.componentmodel;

import java.util.*;
import com.strobel.annotations.*;
import com.strobel.core.*;

final class DictionaryKeyMap implements FrugalKeyMap
{
    private final Map<Integer, Object> _map;
    
    DictionaryKeyMap(final DictionaryKeyMap oldMap, final int excludeIndex) {
        super();
        this._map = new HashMap<Integer, Object>((excludeIndex < 0) ? oldMap._map.size() : (oldMap._map.size() - 1));
        for (final Integer keyIndex : oldMap._map.keySet()) {
            if (keyIndex != excludeIndex) {
                this._map.put(keyIndex, oldMap._map);
            }
        }
    }
    
    DictionaryKeyMap(final int[] keyIndexes, final int newKey, final Object[] values, final Object newValue) {
        super();
        assert newKey >= 0;
        this._map = new HashMap<Integer, Object>(keyIndexes.length + 1);
        for (int i = 0; i < keyIndexes.length; ++i) {
            this._map.put(keyIndexes[i], values[i]);
        }
        this._map.put(newKey, newValue);
        assert this._map.size() > 8;
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap plus(@NotNull final Key<V> key, @NotNull final V value) {
        VerifyArgument.notNull(key, "key");
        VerifyArgument.notNull(value, "value");
        final int keyIndex = key.hashCode();
        final V oldValue = (V)this._map.get(keyIndex);
        if (oldValue == value) {
            return this;
        }
        final DictionaryKeyMap newMap = new DictionaryKeyMap(this, -1);
        newMap._map.put(keyIndex, value);
        return newMap;
    }
    
    @NotNull
    @Override
    public final <V> FrugalKeyMap minus(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        final int keyIndex = key.hashCode();
        if (!this._map.containsKey(keyIndex)) {
            return this;
        }
        final int oldSize = this._map.size();
        final int newSize = oldSize - 1;
        if (newSize > 8) {
            return new DictionaryKeyMap(this, keyIndex);
        }
        final int[] newKeys = new int[newSize];
        final Object[] newValues = new Object[newSize];
        int currentIndex = 0;
        for (final Integer oldKey : this._map.keySet()) {
            if (oldKey != keyIndex) {
                final int i = currentIndex++;
                newKeys[i] = oldKey;
                newValues[i] = this._map.get(oldKey);
            }
        }
        return new ArrayKeyMap(newKeys, newValues);
    }
    
    @Override
    public final <V> V get(@NotNull final Key<V> key) {
        VerifyArgument.notNull(key, "key");
        return (V)this._map.get(key.hashCode());
    }
    
    @Override
    public final boolean isEmpty() {
        return false;
    }
}
