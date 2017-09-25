package com.strobel.collections;

import com.strobel.annotations.*;
import java.util.*;

public class ListBuffer<A> extends AbstractQueue<A>
{
    public ImmutableList<A> elements;
    public ImmutableList<A> last;
    public int count;
    public boolean shared;
    
    public static <T> ListBuffer<T> lb() {
        return new ListBuffer<T>();
    }
    
    public static <T> ListBuffer<T> of(final T x) {
        final ListBuffer<T> lb = new ListBuffer<T>();
        lb.add(x);
        return lb;
    }
    
    public ListBuffer() {
        super();
        this.clear();
    }
    
    @Override
    public final void clear() {
        this.elements = new ImmutableList<A>(null, null);
        this.last = this.elements;
        this.count = 0;
        this.shared = false;
    }
    
    public int length() {
        return this.count;
    }
    
    @Override
    public int size() {
        return this.count;
    }
    
    @Override
    public boolean isEmpty() {
        return this.count == 0;
    }
    
    public boolean nonEmpty() {
        return this.count != 0;
    }
    
    private void copy() {
        final ImmutableList loc_0 = new ImmutableList((A)this.elements.head, this.elements.tail);
        this.elements = loc_0;
        ImmutableList<A> p = loc_0;
        while (true) {
            ImmutableList<A> tail = p.tail;
            if (tail == null) {
                break;
            }
            tail = new ImmutableList<A>(tail.head, tail.tail);
            p.setTail(tail);
            p = tail;
        }
        this.last = p;
        this.shared = false;
    }
    
    public ListBuffer<A> prepend(final A x) {
        this.elements = this.elements.prepend(x);
        ++this.count;
        return this;
    }
    
    public ListBuffer<A> append(final A x) {
        x.getClass();
        if (this.shared) {
            this.copy();
        }
        this.last.head = x;
        this.last.setTail(new ImmutableList<A>(null, null));
        this.last = this.last.tail;
        ++this.count;
        return this;
    }
    
    public ListBuffer<A> appendList(ImmutableList<A> xs) {
        while (xs.nonEmpty()) {
            this.append(xs.head);
            xs = xs.tail;
        }
        return this;
    }
    
    public ListBuffer<A> appendList(final ListBuffer<A> xs) {
        return this.appendList(xs.toList());
    }
    
    public ListBuffer<A> appendArray(final A[] xs) {
        for (final A x : xs) {
            this.append(x);
        }
        return this;
    }
    
    public ImmutableList<A> toList() {
        this.shared = true;
        return this.elements;
    }
    
    @Override
    public boolean contains(final Object x) {
        return this.elements.contains(x);
    }
    
    @NotNull
    @Override
    public <T> T[] toArray(final T[] vec) {
        return this.elements.toArray(vec);
    }
    
    @NotNull
    @Override
    public Object[] toArray() {
        return this.toArray(new Object[this.size()]);
    }
    
    public A first() {
        return this.elements.head;
    }
    
    public A next() {
        final A x = this.elements.head;
        if (this.elements != this.last) {
            this.elements = this.elements.tail;
            --this.count;
        }
        return x;
    }
    
    @NotNull
    @Override
    public Iterator<A> iterator() {
        return new Iterator<A>() {
            ImmutableList<A> elements = ListBuffer.this.elements;
            
            @Override
            public boolean hasNext() {
                return this.elements != ListBuffer.this.last;
            }
            
            @Override
            public A next() {
                if (this.elements == ListBuffer.this.last) {
                    throw new NoSuchElementException();
                }
                final A elem = this.elements.head;
                this.elements = this.elements.tail;
                return elem;
            }
            
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }
    
    @Override
    public boolean add(final A a) {
        this.append(a);
        return true;
    }
    
    @Override
    public boolean remove(final Object o) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean containsAll(final Collection<?> c) {
        for (final Object x : c) {
            if (!this.contains(x)) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public boolean addAll(final Collection<? extends A> c) {
        for (final A a : c) {
            this.append(a);
        }
        return true;
    }
    
    @Override
    public boolean removeAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean retainAll(final Collection<?> c) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean offer(final A a) {
        this.append(a);
        return true;
    }
    
    @Override
    public A poll() {
        return this.next();
    }
    
    @Override
    public A peek() {
        return this.first();
    }
}
