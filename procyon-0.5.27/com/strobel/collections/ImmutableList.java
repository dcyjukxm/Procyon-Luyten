package com.strobel.collections;

import com.strobel.annotations.*;
import java.lang.reflect.*;
import java.util.*;

public class ImmutableList<A> extends AbstractCollection<A> implements List<A>
{
    public A head;
    public ImmutableList<A> tail;
    private static final ImmutableList<?> EMPTY_LIST;
    private static final Iterator<?> EMPTY_ITERATOR;
    
    static {
        EMPTY_LIST = new ImmutableList<Object>((ImmutableList)null) {
            @Override
            public ImmutableList<Object> setTail(final ImmutableList<Object> tail) {
                throw new UnsupportedOperationException();
            }
            
            @Override
            public boolean isEmpty() {
                return true;
            }
        };
        EMPTY_ITERATOR = new Iterator<Object>() {
            @Override
            public boolean hasNext() {
                return false;
            }
            
            @Override
            public Object next() {
                throw new NoSuchElementException();
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    ImmutableList(final A head, final ImmutableList<A> tail) {
        super();
        this.tail = tail;
        this.head = head;
    }
    
    public static <A> ImmutableList<A> empty() {
        return ImmutableList.EMPTY_LIST;
    }
    
    public static <A> ImmutableList<A> of(final A x1) {
        return new ImmutableList<A>(x1, empty());
    }
    
    @SafeVarargs
    public static <A> ImmutableList<A> of(final A x1, final A... rest) {
        return new ImmutableList<A>(x1, from(rest));
    }
    
    public static <A> ImmutableList<A> of(final A x1, final A x2) {
        return new ImmutableList<A>(x1, of(x2));
    }
    
    public static <A> ImmutableList<A> of(final A x1, final A x2, final A x3) {
        return new ImmutableList<A>(x1, of(x2, x3));
    }
    
    public static <A> ImmutableList<A> of(final A x1, final A x2, final A x3, final A... rest) {
        return new ImmutableList<A>(x1, new ImmutableList<A>(x2, new ImmutableList<A>(x3, from(rest))));
    }
    
    public static <A> ImmutableList<A> from(final A[] array) {
        ImmutableList<A> xs = empty();
        if (array != null) {
            for (int i = array.length - 1; i >= 0; --i) {
                xs = new ImmutableList<A>(array[i], xs);
            }
        }
        return xs;
    }
    
    @Deprecated
    public static <A> ImmutableList<A> fill(final int len, final A init) {
        ImmutableList<A> l = empty();
        for (int i = 0; i < len; ++i) {
            l = new ImmutableList<A>(init, l);
        }
        return l;
    }
    
    @Override
    public boolean isEmpty() {
        return this.tail == null;
    }
    
    public boolean nonEmpty() {
        return this.tail != null;
    }
    
    public int length() {
        ImmutableList<A> l;
        int len;
        for (l = this, len = 0; l.tail != null; l = l.tail, ++len) {}
        return len;
    }
    
    @Override
    public int size() {
        return this.length();
    }
    
    public ImmutableList<A> setTail(final ImmutableList<A> tail) {
        return this.tail = tail;
    }
    
    public ImmutableList<A> prepend(final A x) {
        return new ImmutableList<A>(x, this);
    }
    
    public ImmutableList<A> prependList(final ImmutableList<A> xs) {
        if (this.isEmpty()) {
            return xs;
        }
        if (xs.isEmpty()) {
            return this;
        }
        if (xs.tail.isEmpty()) {
            return this.prepend(xs.head);
        }
        ImmutableList<A> result = this;
        ImmutableList<A> rev = xs.reverse();
        assert rev != xs;
        while (rev.nonEmpty()) {
            final ImmutableList<A> h = rev;
            rev = rev.tail;
            h.setTail(result);
            result = h;
        }
        return result;
    }
    
    public ImmutableList<A> reverse() {
        if (this.isEmpty() || this.tail.isEmpty()) {
            return this;
        }
        ImmutableList<A> rev = empty();
        for (ImmutableList<A> l = this; l.nonEmpty(); l = l.tail) {
            rev = new ImmutableList<A>(l.head, rev);
        }
        return rev;
    }
    
    public ImmutableList<A> append(final A x) {
        return of(x).prependList(this);
    }
    
    public ImmutableList<A> appendList(final ImmutableList<A> x) {
        return x.prependList(this);
    }
    
    public ImmutableList<A> appendList(final ListBuffer<A> x) {
        return this.appendList(x.toList());
    }
    
    @NotNull
    @Override
    public <T> T[] toArray(@NotNull final T[] vec) {
        int i;
        ImmutableList<A> l;
        for (i = 0, l = this; l.nonEmpty() && i < vec.length; l = l.tail, ++i) {
            vec[i] = (T)l.head;
        }
        if (l.isEmpty()) {
            if (i < vec.length) {
                vec[i] = null;
            }
            return vec;
        }
        return (T[])this.toArray((Object[])Array.newInstance(vec.getClass().getComponentType(), this.size()));
    }
    
    @NotNull
    @Override
    public Object[] toArray() {
        return this.toArray(new Object[this.size()]);
    }
    
    public String toString(final String sep) {
        if (this.isEmpty()) {
            return "";
        }
        final StringBuilder buffer = new StringBuilder();
        buffer.append(this.head);
        for (ImmutableList<A> l = this.tail; l.nonEmpty(); l = l.tail) {
            buffer.append(sep);
            buffer.append(l.head);
        }
        return buffer.toString();
    }
    
    @Override
    public String toString() {
        return this.toString(",");
    }
    
    @Override
    public int hashCode() {
        ImmutableList<A> l = this;
        int h = 1;
        while (l.tail != null) {
            h = h * 31 + ((l.head == null) ? 0 : l.head.hashCode());
            l = l.tail;
        }
        return h;
    }
    
    @Override
    public boolean equals(final Object other) {
        if (other instanceof ImmutableList) {
            return equals(this, (ImmutableList)other);
        }
        if (other instanceof List) {
            ImmutableList<A> t;
            Iterator<?> it;
            Object o;
            for (t = this, it = ((List)other).iterator(); t.tail != null && it.hasNext(); t = t.tail) {
                o = it.next();
                if (t.head == null) {
                    if (o != null) {
                        return false;
                    }
                }
                else if (!t.head.equals(o)) {
                    return false;
                }
            }
            return t.isEmpty() && !it.hasNext();
        }
        return false;
    }
    
    public static boolean equals(ImmutableList<?> xs, ImmutableList<?> ys) {
        while (xs.tail != null && ys.tail != null) {
            if (xs.head == null) {
                if (ys.head != null) {
                    return false;
                }
            }
            else if (!xs.head.equals(ys.head)) {
                return false;
            }
            xs = xs.tail;
            ys = ys.tail;
        }
        return xs.tail == null && ys.tail == null;
    }
    
    @Override
    public boolean contains(final Object x) {
        for (ImmutableList<A> l = this; l.tail != null; l = l.tail) {
            if (x == null) {
                if (l.head == null) {
                    return true;
                }
            }
            else if (l.head.equals(x)) {
                return true;
            }
        }
        return false;
    }
    
    public A last() {
        A last = null;
        for (ImmutableList<A> t = this; t.tail != null; t = t.tail) {
            last = t.head;
        }
        return last;
    }
    
    public static <T> ImmutableList<T> convert(final Class<T> type, final ImmutableList<?> list) {
        if (list == null) {
            return null;
        }
        for (final Object o : list) {
            type.cast(o);
        }
        return list;
    }
    
    private static <A> Iterator<A> emptyIterator() {
        return ImmutableList.EMPTY_ITERATOR;
    }
    
    @NotNull
    @Override
    public Iterator<A> iterator() {
        if (this.tail == null) {
            return emptyIterator();
        }
        return new Iterator<A>() {
            private ImmutableList<A> _elements = ImmutableList.this;
            
            @Override
            public boolean hasNext() {
                return this._elements.tail != null;
            }
            
            @Override
            public A next() {
                if (this._elements.tail == null) {
                    throw new NoSuchElementException();
                }
                final A result = this._elements.head;
                this._elements = this._elements.tail;
                return result;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public A get(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException(String.valueOf(index));
        }
        ImmutableList<A> l = this;
        for (int i = index; i-- > 0 && !l.isEmpty(); l = l.tail) {}
        if (l.isEmpty()) {
            throw new IndexOutOfBoundsException("Index: " + index + ", " + "Size: " + this.size());
        }
        return l.head;
    }
    
    @Override
    public boolean addAll(final int index, @NotNull final Collection<? extends A> c) {
        if (c.isEmpty()) {
            return false;
        }
        throw new UnsupportedOperationException();
    }
    
    @Override
    public A set(final int index, final A element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void add(final int index, final A element) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public A remove(final int index) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public int indexOf(final Object o) {
        int i = 0;
        for (ImmutableList<A> l = this; l.tail != null; l = l.tail, ++i) {
            if (l.head == null) {
                if (o == null) {
                    return i;
                }
            }
            else if (l.head.equals(o)) {
                return i;
            }
        }
        return -1;
    }
    
    @Override
    public int lastIndexOf(final Object o) {
        int last = -1;
        int i = 0;
        for (ImmutableList<A> l = this; l.tail != null; l = l.tail, ++i) {
            if (l.head == null) {
                if (o != null) {
                    continue;
                }
            }
            else if (!l.head.equals(o)) {
                continue;
            }
            last = i;
        }
        return last;
    }
    
    @NotNull
    @Override
    public ListIterator<A> listIterator() {
        return Collections.unmodifiableList((List<? extends A>)new ArrayList<A>((Collection<? extends A>)this)).listIterator();
    }
    
    @NotNull
    @Override
    public ListIterator<A> listIterator(final int index) {
        return Collections.unmodifiableList((List<? extends A>)new ArrayList<A>((Collection<? extends A>)this)).listIterator(index);
    }
    
    @NotNull
    @Override
    public List<A> subList(final int fromIndex, final int toIndex) {
        if (fromIndex < 0 || toIndex > this.size() || fromIndex > toIndex) {
            throw new IllegalArgumentException();
        }
        final ArrayList<A> a = new ArrayList<A>(toIndex - fromIndex);
        int i = 0;
        for (ImmutableList<A> l = this; l.tail != null && i != toIndex; l = l.tail, ++i) {
            if (i >= fromIndex) {
                a.add(l.head);
            }
        }
        return Collections.unmodifiableList((List<? extends A>)a);
    }
}
