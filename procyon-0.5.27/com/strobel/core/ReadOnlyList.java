package com.strobel.core;

import java.lang.reflect.*;
import com.strobel.annotations.*;
import com.strobel.util.*;
import java.util.*;

public class ReadOnlyList<T> implements IReadOnlyList<T>, List<T>, RandomAccess
{
    private static final ReadOnlyList<?> EMPTY;
    private final int _offset;
    private final int _length;
    private final T[] _elements;
    
    static {
        EMPTY = new ReadOnlyList<Object>((Object[])new Object[0]);
    }
    
    public static <T> ReadOnlyList<T> emptyList() {
        return ReadOnlyList.EMPTY;
    }
    
    public ReadOnlyList(final T... elements) {
        super();
        VerifyArgument.notNull(elements, "elements");
        this._offset = 0;
        this._length = elements.length;
        this._elements = Arrays.copyOf(elements, elements.length, elements.getClass());
    }
    
    public ReadOnlyList(final Class<? extends T> elementType, final Collection<? extends T> elements) {
        super();
        VerifyArgument.notNull(elementType, "elementType");
        VerifyArgument.notNull(elements, "elements");
        this._offset = 0;
        this._length = elements.size();
        this._elements = elements.toArray((Object[])Array.newInstance(elementType, this._length));
    }
    
    public ReadOnlyList(final T[] elements, final int offset, final int length) {
        super();
        VerifyArgument.notNull(elements, "elements");
        this._elements = Arrays.copyOf(elements, elements.length, elements.getClass());
        subListRangeCheck(offset, offset + length, this._elements.length);
        this._offset = offset;
        this._length = length;
    }
    
    protected ReadOnlyList<T> newInstance() {
        return new ReadOnlyList<T>((T[])this._elements, this._offset, this._length);
    }
    
    private ReadOnlyList(final ReadOnlyList<T> baseList, final int offset, final int length) {
        super();
        VerifyArgument.notNull(baseList, "baseList");
        final Object[] elements = baseList._elements;
        subListRangeCheck(offset, offset + length, elements.length);
        this._elements = elements;
        this._offset = offset;
        this._length = length;
    }
    
    protected final int getOffset() {
        return this._offset;
    }
    
    protected final T[] getElements() {
        return (T[])this._elements;
    }
    
    @Override
    public final int size() {
        return this._length;
    }
    
    @Override
    public final boolean isEmpty() {
        return this.size() == 0;
    }
    
    @Override
    public boolean containsAll(final Iterable<? extends T> c) {
        VerifyArgument.notNull(c, "c");
        for (final T element : c) {
            if (!ArrayUtilities.contains(this._elements, element)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public final boolean contains(final Object o) {
        return this.indexOf(o) != -1;
    }
    
    @NotNull
    @Override
    public final Iterator<T> iterator() {
        return new ReadOnlyCollectionIterator();
    }
    
    @NotNull
    @Override
    public final T[] toArray() {
        if (this._length == 0) {
            return EmptyArrayCache.fromArrayType(this._elements.getClass());
        }
        return Arrays.copyOfRange(this._elements, this._offset, this._offset + this._length, this._elements.getClass());
    }
    
    @NotNull
    @Override
    public final <T> T[] toArray(@NotNull final T[] a) {
        final int length = this._length;
        if (a.length < length) {
            return Arrays.copyOfRange(this._elements, this._offset, this._offset + this._length, this._elements.getClass());
        }
        System.arraycopy(this._elements, this._offset, a, 0, length);
        if (a.length > length) {
            a[length] = null;
        }
        return a;
    }
    
    @Override
    public final boolean add(final T T) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final boolean remove(final Object o) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final boolean containsAll(@NotNull final Collection<?> c) {
        for (final Object o : c) {
            if (!this.contains(o)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public final boolean addAll(@NotNull final Collection<? extends T> c) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final boolean addAll(final int index, @NotNull final Collection<? extends T> c) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final boolean removeAll(@NotNull final Collection<?> c) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final boolean retainAll(@NotNull final Collection<?> c) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final void clear() {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final T get(final int index) {
        return (T)this._elements[this._offset + index];
    }
    
    @Override
    public final T set(final int index, final T element) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final void add(final int index, final T element) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public final T remove(final int index) {
        throw Error.unmodifiableCollection();
    }
    
    @Override
    public int hashCode() {
        int hash = 0;
        for (int i = this._offset, n = this._offset + this._length; i < n; ++i) {
            final T element = (T)this._elements[i];
            if (element != null) {
                hash = hash * 31 + element.hashCode();
            }
        }
        return hash;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ReadOnlyList)) {
            return false;
        }
        final ReadOnlyList<T> other = (ReadOnlyList<T>)obj;
        return Arrays.equals(this._elements, other._elements);
    }
    
    @Override
    public final int indexOf(final Object o) {
        final Object[] elements = this._elements;
        final int start = this._offset;
        final int end = start + this._length;
        if (o == null) {
            for (int i = start; i < end; ++i) {
                if (elements[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = start; i < end; ++i) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    @Override
    public final int lastIndexOf(final Object o) {
        final Object[] elements = this._elements;
        final int start = this._offset;
        final int end = start + this._length;
        if (o == null) {
            for (int i = end - 1; i >= start; --i) {
                if (elements[i] == null) {
                    return i;
                }
            }
        }
        else {
            for (int i = end - 1; i >= start; --i) {
                if (o.equals(elements[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    @Override
    public String toString() {
        final Iterator<T> it = this.iterator();
        if (!it.hasNext()) {
            return "[]";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        while (true) {
            final T e = it.next();
            sb.append((e == this) ? "(this Collection)" : e);
            if (!it.hasNext()) {
                break;
            }
            sb.append(',').append(' ');
        }
        return sb.append(']').toString();
    }
    
    @NotNull
    @Override
    public final ListIterator<T> listIterator() {
        return new ReadOnlyCollectionIterator();
    }
    
    @NotNull
    @Override
    public final ListIterator<T> listIterator(final int index) {
        return new ReadOnlyCollectionIterator(index);
    }
    
    protected static void subListRangeCheck(final int fromIndex, final int toIndex, final int size) {
        if (fromIndex < 0) {
            throw new IndexOutOfBoundsException("fromIndex = " + fromIndex);
        }
        if (toIndex > size) {
            throw new IndexOutOfBoundsException("toIndex = " + toIndex);
        }
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
    }
    
    @NotNull
    @Override
    public ReadOnlyList<T> subList(final int fromIndex, final int toIndex) {
        subListRangeCheck(fromIndex, toIndex, this.size());
        return new ReadOnlyList<T>(this, this._offset + fromIndex, this._offset + toIndex);
    }
    
    private final class ReadOnlyCollectionIterator implements ListIterator<T>
    {
        private int _position;
        
        ReadOnlyCollectionIterator() {
            super();
            this._position = -1;
        }
        
        ReadOnlyCollectionIterator(final int startPosition) {
            super();
            this._position = -1;
            if (startPosition < -1 || startPosition >= ReadOnlyList.this.size()) {
                throw new IllegalArgumentException();
            }
            this._position = startPosition;
        }
        
        @Override
        public final boolean hasNext() {
            return this._position + 1 < ReadOnlyList.this.size();
        }
        
        @Override
        public final T next() {
            if (!this.hasNext()) {
                throw new IllegalStateException();
            }
            return ReadOnlyList.this.get(++this._position);
        }
        
        @Override
        public final boolean hasPrevious() {
            return this._position > 0;
        }
        
        @Override
        public final T previous() {
            if (!this.hasPrevious()) {
                throw new IllegalStateException();
            }
            final ReadOnlyList loc_0 = ReadOnlyList.this;
            final int loc_1 = this._position - 1;
            this._position = loc_1;
            return loc_0.get(loc_1);
        }
        
        @Override
        public final int nextIndex() {
            return this._position + 1;
        }
        
        @Override
        public final int previousIndex() {
            return this._position + 1;
        }
        
        @Override
        public final void remove() {
            throw Error.unmodifiableCollection();
        }
        
        @Override
        public final void set(final T T) {
            throw Error.unmodifiableCollection();
        }
        
        @Override
        public final void add(@NotNull final T T) {
            throw Error.unmodifiableCollection();
        }
    }
}
