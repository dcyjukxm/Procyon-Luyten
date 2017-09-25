package com.strobel.collections.concurrent;

import com.strobel.annotations.*;
import java.lang.ref.*;
import com.strobel.core.*;

public final class ConcurrentWeakIntObjectHashMap<V> extends ConcurrentRefValueIntObjectHashMap<V>
{
    @Override
    protected final IntReference<V> createReference(final int key, @NotNull final V value, final ReferenceQueue<V> queue) {
        return new WeakIntReference<V>(key, value, queue);
    }
    
    private static final class WeakIntReference<V> extends WeakReference<V> implements IntReference<V>
    {
        private final int _hash;
        private final int _key;
        
        WeakIntReference(final int key, final V referent, final ReferenceQueue<? super V> q) {
            super(referent, q);
            this._key = key;
            this._hash = referent.hashCode();
        }
        
        @Override
        public final int key() {
            return this._key;
        }
        
        @Override
        public final int hashCode() {
            return this._hash;
        }
        
        @Override
        public final boolean equals(final Object obj) {
            if (!(obj instanceof WeakIntReference)) {
                return false;
            }
            final WeakIntReference<V> other = (WeakIntReference<V>)obj;
            return other._hash == this._hash && Comparer.equals(other.get(), this.get());
        }
    }
}
