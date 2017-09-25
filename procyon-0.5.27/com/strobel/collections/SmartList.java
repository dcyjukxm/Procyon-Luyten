package com.strobel.collections;

import com.strobel.annotations.*;
import java.util.*;

public class SmartList<E> extends AbstractList<E>
{
    private Object _data;
    private int _size;
    
    public SmartList() {
        super();
        this._data = null;
        this._size = 0;
    }
    
    public SmartList(final E element) {
        super();
        this._data = null;
        this._size = 0;
        this.add(element);
    }
    
    public SmartList(@NotNull final Collection<? extends E> elements) {
        super();
        this._data = null;
        this._size = 0;
        final int size = elements.size();
        if (size == 1) {
            final E element = (E)((elements instanceof List) ? ((List)elements).get(0) : elements.iterator().next());
            this.add(element);
        }
        else if (size > 0) {
            this._size = size;
            this._data = elements.toArray(new Object[size]);
        }
    }
    
    public SmartList(@NotNull final E... elements) {
        super();
        this._data = null;
        this._size = 0;
        if (elements.length == 1) {
            this.add(elements[0]);
        }
        else if (elements.length > 0) {
            this._size = elements.length;
            this._data = Arrays.copyOf(elements, this._size);
        }
    }
    
    @Override
    public E get(final int index) {
        if (index < 0 || index >= this._size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this._size);
        }
        if (this._size == 1) {
            return (E)this._data;
        }
        return (E)((Object[])this._data)[index];
    }
    
    @Override
    public boolean add(final E e) {
        switch (this._size) {
            case 0: {
                this._data = e;
                break;
            }
            case 1: {
                final Object[] array = { this._data, e };
                this._data = array;
                break;
            }
            default: {
                Object[] array = (Object[])this._data;
                final int oldCapacity = array.length;
                if (this._size >= oldCapacity) {
                    int newCapacity = oldCapacity * 3 / 2 + 1;
                    final int minCapacity = this._size + 1;
                    if (newCapacity < minCapacity) {
                        newCapacity = minCapacity;
                    }
                    final Object[] oldArray = array;
                    array = (Object[])(this._data = new Object[newCapacity]);
                    System.arraycopy(oldArray, 0, array, 0, oldCapacity);
                }
                array[this._size] = e;
                break;
            }
        }
        ++this._size;
        ++this.modCount;
        return true;
    }
    
    @Override
    public void add(final int index, final E e) {
        if (index < 0 || index > this._size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this._size);
        }
        Label_0183: {
            switch (this._size) {
                case 0: {
                    this._data = e;
                    break Label_0183;
                }
                case 1: {
                    if (index == 0) {
                        final Object[] array = { e, this._data };
                        this._data = array;
                        break Label_0183;
                    }
                    break;
                }
            }
            final Object[] array = new Object[this._size + 1];
            if (this._size == 1) {
                array[0] = this._data;
            }
            else {
                final Object[] oldArray = (Object[])this._data;
                System.arraycopy(oldArray, 0, array, 0, index);
                System.arraycopy(oldArray, index, array, index + 1, this._size - index);
            }
            array[index] = e;
            this._data = array;
        }
        ++this._size;
        ++this.modCount;
    }
    
    @Override
    public int size() {
        return this._size;
    }
    
    @Override
    public void clear() {
        this._data = null;
        this._size = 0;
        ++this.modCount;
    }
    
    @Override
    public E set(final int index, final E element) {
        if (index < 0 || index >= this._size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this._size);
        }
        E oldValue;
        if (this._size == 1) {
            oldValue = (E)this._data;
            this._data = element;
        }
        else {
            final Object[] array = (Object[])this._data;
            oldValue = (E)array[index];
            array[index] = element;
        }
        return oldValue;
    }
    
    @Override
    public E remove(final int index) {
        if (index < 0 || index >= this._size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + this._size);
        }
        E oldValue;
        if (this._size == 1) {
            oldValue = (E)this._data;
            this._data = null;
        }
        else {
            final Object[] array = (Object[])this._data;
            oldValue = (E)array[index];
            if (this._size == 2) {
                this._data = array[1 - index];
            }
            else {
                final int numMoved = this._size - index - 1;
                if (numMoved > 0) {
                    System.arraycopy(array, index + 1, array, index, numMoved);
                }
                array[this._size - 1] = null;
            }
        }
        --this._size;
        ++this.modCount;
        return oldValue;
    }
    
    @NotNull
    @Override
    public Iterator<E> iterator() {
        switch (this._size) {
            case 0: {
                return Collections.emptyIterator();
            }
            case 1: {
                return new SingletonIterator();
            }
            default: {
                return super.iterator();
            }
        }
    }
    
    @Override
    public void sort(@NotNull final Comparator<? super E> comparator) {
        if (this._size >= 2) {
            Arrays.sort((Object[])this._data, 0, this._size, comparator);
        }
    }
    
    public int getModificationCount() {
        return this.modCount;
    }
    
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull final T[] a) {
        if (this._size == 1) {
            final int length = a.length;
            if (length != 0) {
                a[0] = (T)this._data;
                if (length > 1) {
                    a[1] = null;
                }
                return a;
            }
        }
        return super.toArray(a);
    }
    
    public void trimToSize() {
        if (this._size < 2) {
            return;
        }
        final Object[] array = (Object[])this._data;
        final int oldCapacity = array.length;
        if (this._size < oldCapacity) {
            ++this.modCount;
            this._data = Arrays.copyOf(array, this._size);
        }
    }
    
    static /* synthetic */ int access$0(final SmartList param_0) {
        return param_0.modCount;
    }
    
    static /* synthetic */ Object access$1(final SmartList param_0) {
        return param_0._data;
    }
    
    private final class SingletonIterator implements Iterator<E>
    {
        private boolean _visited;
        private final int _initialModCount;
        
        public SingletonIterator() {
            super();
            this._initialModCount = SmartList.access$0(SmartList.this);
        }
        
        @Override
        public boolean hasNext() {
            return !this._visited;
        }
        
        @Override
        public E next() {
            if (this._visited) {
                throw new NoSuchElementException();
            }
            this._visited = true;
            if (SmartList.access$0(SmartList.this) != this._initialModCount) {
                throw new ConcurrentModificationException("ModCount: " + SmartList.access$0(SmartList.this) + "; expected: " + this._initialModCount);
            }
            return (E)SmartList.access$1(SmartList.this);
        }
        
        @Override
        public void remove() {
            if (SmartList.access$0(SmartList.this) != this._initialModCount) {
                throw new ConcurrentModificationException("ModCount: " + SmartList.access$0(SmartList.this) + "; expected: " + this._initialModCount);
            }
            SmartList.this.clear();
        }
    }
}
