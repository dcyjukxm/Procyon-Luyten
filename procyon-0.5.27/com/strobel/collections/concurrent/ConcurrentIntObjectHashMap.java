package com.strobel.collections.concurrent;

import com.strobel.concurrent.*;
import com.strobel.core.*;
import com.strobel.annotations.*;
import java.util.*;

public class ConcurrentIntObjectHashMap<V> implements ConcurrentIntObjectMap<V>
{
    protected static final int DEFAULT_INITIAL_CAPACITY = 16;
    protected static final int MAXIMUM_CAPACITY = 1073741824;
    protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
    private static final StripedReentrantLock STRIPED_REENTRANT_LOCK;
    private final byte _lockIndex;
    protected volatile IntHashEntry<V>[] table;
    protected volatile int count;
    protected int modCount;
    private final float _loadFactor;
    
    static {
        STRIPED_REENTRANT_LOCK = StripedReentrantLock.instance();
    }
    
    public ConcurrentIntObjectHashMap() {
        this(16, 0.75f);
    }
    
    public ConcurrentIntObjectHashMap(final int initialCapacity) {
        this(initialCapacity, 0.75f);
    }
    
    public ConcurrentIntObjectHashMap(final int initialCapacity, final float loadFactor) {
        super();
        this._lockIndex = (byte)ConcurrentIntObjectHashMap.STRIPED_REENTRANT_LOCK.allocateLockIndex();
        final int capacity = computeInitialCapacity(initialCapacity, loadFactor);
        this.setTable(new IntHashEntry[capacity]);
        this._loadFactor = loadFactor;
    }
    
    private void lock() {
        ConcurrentIntObjectHashMap.STRIPED_REENTRANT_LOCK.lock(this._lockIndex);
    }
    
    private void unlock() {
        ConcurrentIntObjectHashMap.STRIPED_REENTRANT_LOCK.unlock(this._lockIndex);
    }
    
    private int threshold() {
        return (int)(this.table.length * this._loadFactor);
    }
    
    private void setTable(final IntHashEntry<?>[] newTable) {
        this.table = newTable;
    }
    
    private static int computeInitialCapacity(final int initialCapacity, final float loadFactor) {
        VerifyArgument.isNonNegative(initialCapacity, "initialCapacity");
        VerifyArgument.isPositive(loadFactor, "loadFactor");
        int desiredCapacity;
        int capacity;
        for (desiredCapacity = Math.min(initialCapacity, 1073741824), capacity = 1; capacity < desiredCapacity; capacity <<= 1) {}
        return capacity;
    }
    
    private IntHashEntry<V> getFirst(final int hash) {
        final IntHashEntry[] t = this.table;
        return t[hash & t.length - 1];
    }
    
    private V readValueUnderLock(final IntHashEntry<V> entry) {
        this.lock();
        try {
            return entry.value;
        }
        finally {
            this.unlock();
        }
    }
    
    private void rehash() {
        final IntHashEntry[] oldTable = this.table;
        final int oldCapacity = oldTable.length;
        if (oldCapacity >= 1073741824) {
            return;
        }
        final int newCapacity = oldCapacity << 1;
        final IntHashEntry[] newTable = new IntHashEntry[newCapacity];
        final int sizeMask = newCapacity - 1;
        IntHashEntry[] loc_1;
        for (int loc_0 = (loc_1 = oldTable).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final IntHashEntry oldEntry = loc_1[loc_2];
            if (oldEntry != null) {
                final IntHashEntry<V> next = oldEntry.next;
                final int index = oldEntry.key & sizeMask;
                if (next == null) {
                    newTable[index] = oldEntry;
                }
                else {
                    IntHashEntry<V> lastRun = oldEntry;
                    int lastIndex = index;
                    for (IntHashEntry<V> last = next; last != null; last = last.next) {
                        final int k = last.key & sizeMask;
                        if (k != lastIndex) {
                            lastIndex = k;
                            lastRun = last;
                        }
                    }
                    newTable[lastIndex] = lastRun;
                    for (IntHashEntry<V> p = oldEntry; p != lastRun; p = p.next) {
                        final int currentIndex = p.key & sizeMask;
                        final IntHashEntry<V> current = newTable[currentIndex];
                        newTable[currentIndex] = new IntHashEntry(p.key, current, p.value, null);
                    }
                }
            }
        }
        this.setTable(newTable);
    }
    
    protected V put(final int key, @NotNull final V value, final boolean onlyIfAbsent) {
        this.lock();
        try {
            int c = this.count;
            if (c++ > this.threshold()) {
                this.rehash();
            }
            final IntHashEntry[] t = this.table;
            final int index = key & this.table.length - 1;
            IntHashEntry<V> entry;
            IntHashEntry<V> first;
            for (first = (entry = t[index]); entry != null && entry.key != key; entry = entry.next) {}
            V oldValue;
            if (entry != null) {
                oldValue = entry.value;
                if (!onlyIfAbsent) {
                    entry.value = value;
                }
            }
            else {
                oldValue = null;
                ++this.modCount;
                t[index] = new IntHashEntry(key, first, value, null);
                this.count = c;
            }
            return oldValue;
        }
        finally {
            this.unlock();
        }
    }
    
    protected V removeCore(final int key, @Nullable final V value) {
        this.lock();
        try {
            final int newCount = this.count - 1;
            final IntHashEntry[] t = this.table;
            final int index = key & this.table.length - 1;
            IntHashEntry<V> entry;
            for (entry = t[index]; entry != null && entry.key != key; entry = entry.next) {}
            if (entry != null) {
                final V oldValue = entry.value;
                if (value == null || value.equals(oldValue)) {
                    ++this.modCount;
                    IntHashEntry<V> p;
                    IntHashEntry<V> newFirst;
                    for (newFirst = (p = t[index]); p != entry; p = p.next) {
                        newFirst = new IntHashEntry<V>(p.key, newFirst, p.value, null);
                    }
                    t[index] = newFirst;
                    this.count = newCount;
                    return oldValue;
                }
            }
            return null;
        }
        finally {
            this.unlock();
        }
    }
    
    @NotNull
    @Override
    public V addOrGet(final int key, @NotNull final V value) {
        final V previous = this.putIfAbsent(key, value);
        return (previous != null) ? previous : value;
    }
    
    @Override
    public boolean remove(final int key, @NotNull final V value) {
        return this.removeCore(key, value) != null;
    }
    
    @Override
    public boolean replace(final int key, @NotNull final V oldValue, @NotNull final V newValue) {
        VerifyArgument.notNull(oldValue, "oldValue");
        VerifyArgument.notNull(newValue, "newValue");
        this.lock();
        try {
            IntHashEntry<V> entry;
            for (entry = this.getFirst(key); entry != null && entry.key != key; entry = entry.next) {}
            if (entry != null && oldValue.equals(entry.value)) {
                entry.value = newValue;
                return true;
            }
            return false;
        }
        finally {
            this.unlock();
        }
    }
    
    @Override
    public V put(final int key, @NotNull final V value) {
        return this.put(key, value, false);
    }
    
    @Override
    public V putIfAbsent(final int key, @NotNull final V value) {
        return this.put(key, value, true);
    }
    
    @Override
    public V get(final int key) {
        if (this.count != 0) {
            for (IntHashEntry<V> entry = this.getFirst(key); entry != null; entry = entry.next) {
                if (entry.key == key) {
                    final V value = entry.value;
                    return (V)((value != null) ? value : this.readValueUnderLock(entry));
                }
            }
        }
        return null;
    }
    
    @Override
    public V remove(final int key) {
        return this.removeCore(key, null);
    }
    
    @Override
    public int size() {
        return this.count;
    }
    
    @Override
    public boolean isEmpty() {
        return this.count == 0;
    }
    
    @Override
    public boolean contains(final int key) {
        if (this.count != 0) {
            for (IntHashEntry<V> entry = this.getFirst(key); entry != null; entry = entry.next) {
                if (entry.key == key) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void clear() {
        if (this.count != 0) {
            this.lock();
            try {
                final IntHashEntry[] t = this.table;
                for (int i = 0; i < t.length; ++i) {
                    t[i] = null;
                }
                ++this.modCount;
                this.count = 0;
            }
            finally {
                this.unlock();
            }
            this.unlock();
        }
    }
    
    @NotNull
    @Override
    public int[] keys() {
        final IntHashEntry[] t = this.table;
        final int c = Math.min(this.count, t.length);
        int[] keys = new int[c];
        int k = 0;
        for (int i = 0; i < t.length; ++i, ++k) {
            if (k >= keys.length) {
                keys = Arrays.copyOf(keys, keys.length * 2);
            }
            keys[k] = t[i].key;
        }
        if (k < keys.length) {
            return Arrays.copyOfRange(keys, 0, k);
        }
        return keys;
    }
    
    @NotNull
    @Override
    public Iterable<IntObjectEntry<V>> entries() {
        return new Iterable<IntObjectEntry<V>>() {
            @Override
            public Iterator<IntObjectEntry<V>> iterator() {
                return new Iterator<IntObjectEntry<V>>() {
                    private final HashIterator hashIterator = new HashIterator((HashIterator)null, (HashIterator)null);
                    
                    @Override
                    public final boolean hasNext() {
                        return this.hashIterator.hasNext();
                    }
                    
                    @Override
                    public final IntObjectEntry<V> next() {
                        final IntHashEntry<V> e = this.hashIterator.nextEntry();
                        return new SimpleEntry<V>(e.key, e.value, null);
                    }
                    
                    @Override
                    public final void remove() {
                        this.hashIterator.remove();
                    }
                };
            }
        };
    }
    
    @NotNull
    public Iterable<V> elements() {
        return new Iterable<V>() {
            @Override
            public Iterator<V> iterator() {
                return new ValueIterator((ValueIterator)null);
            }
        };
    }
    
    private class HashIterator
    {
        private int _nextTableIndex;
        private IntHashEntry<V> _nextEntry;
        private IntHashEntry<V> _lastReturned;
        
        private HashIterator() {
            super();
            this._nextTableIndex = ConcurrentIntObjectHashMap.this.table.length - 1;
            this.advance();
        }
        
        private void advance() {
            if (this._nextEntry != null && (this._nextEntry = this._nextEntry.next) != null) {
                return;
            }
            while (this._nextTableIndex >= 0) {
                if ((this._nextEntry = ConcurrentIntObjectHashMap.this.table[this._nextTableIndex--]) != null) {
                    return;
                }
            }
        }
        
        public final boolean hasMoreElements() {
            return this._nextEntry != null;
        }
        
        public final boolean hasNext() {
            return this._nextEntry != null;
        }
        
        protected final IntHashEntry<V> nextEntry() {
            if (this._nextEntry == null) {
                throw new IllegalStateException();
            }
            this._lastReturned = this._nextEntry;
            this.advance();
            return this._lastReturned;
        }
        
        public final void remove() {
            if (this._lastReturned == null) {
                throw new IllegalStateException();
            }
            ConcurrentIntObjectHashMap.this.remove(this._lastReturned.key);
            this._lastReturned = null;
        }
    }
    
    private final class ValueIterator extends HashIterator implements Iterator<V>, Enumeration<V>
    {
        private ValueIterator() {
            super((HashIterator)null);
        }
        
        @Override
        public V nextElement() {
            return this.nextEntry().value;
        }
        
        @Override
        public V next() {
            return this.nextEntry().value;
        }
    }
    
    private static final class SimpleEntry<V> implements IntObjectEntry<V>
    {
        private final int _key;
        private final V _value;
        
        private SimpleEntry(final int key, final V value) {
            super();
            this._key = key;
            this._value = value;
        }
        
        @Override
        public final int key() {
            return this._key;
        }
        
        @NotNull
        @Override
        public final V value() {
            return this._value;
        }
    }
    
    private static final class IntHashEntry<V>
    {
        final int key;
        final IntHashEntry<V> next;
        @NotNull
        volatile V value;
        
        private IntHashEntry(final int key, final IntHashEntry<V> next, @NotNull final V value) {
            super();
            this.key = key;
            this.next = next;
            this.value = value;
        }
    }
}
