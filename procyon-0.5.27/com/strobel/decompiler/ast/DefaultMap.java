package com.strobel.decompiler.ast;

import java.util.*;
import com.strobel.functions.*;
import com.strobel.core.*;

public final class DefaultMap<K, V> extends IdentityHashMap<K, V>
{
    private final Supplier<V> _defaultValueFactory;
    
    public DefaultMap(final Supplier<V> defaultValueFactory) {
        super();
        this._defaultValueFactory = VerifyArgument.notNull(defaultValueFactory, "defaultValueFactory");
    }
    
    @Override
    public V get(final Object key) {
        V value = super.get(key);
        if (value == null) {
            this.put((K)key, value = this._defaultValueFactory.get());
        }
        return value;
    }
}
