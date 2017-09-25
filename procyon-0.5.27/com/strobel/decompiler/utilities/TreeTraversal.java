package com.strobel.decompiler.utilities;

import com.strobel.functions.*;
import java.util.*;
import com.strobel.util.*;
import com.strobel.core.*;

public final class TreeTraversal
{
    public static <T> Iterable<T> preOrder(final T root, final Function<T, Iterable<T>> recursion) {
        return preOrder((Iterable<T>)Collections.singletonList(root), recursion);
    }
    
    public static <T> Iterable<T> preOrder(final Iterable<T> input, final Function<T, Iterable<T>> recursion) {
        return new Iterable<T>() {
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>(input) {
                    final Stack<Iterator<T>> stack;
                    boolean returnedCurrent;
                    T next;
                    
                    {
                        (this.stack = new Stack<Iterator<T>>()).push(param_1.iterator());
                    }
                    
                    private T selectNext() {
                        if (this.next != null) {
                            return this.next;
                        }
                        while (!this.stack.isEmpty()) {
                            if (this.stack.peek().hasNext()) {
                                this.next = this.stack.peek().next();
                                if (this.next != null) {
                                    final Iterable<T> children = recursion.apply(this.next);
                                    if (children != null) {
                                        this.stack.push(children.iterator());
                                    }
                                }
                                return this.next;
                            }
                            this.stack.pop();
                        }
                        return null;
                    }
                    
                    @Override
                    public final boolean hasNext() {
                        return this.selectNext() != null;
                    }
                    
                    @Override
                    public final T next() {
                        final T next = this.selectNext();
                        if (next == null) {
                            throw new NoSuchElementException();
                        }
                        this.next = null;
                        return next;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
        };
    }
    
    public static <T> Iterable<T> postOrder(final T root, final Function<T, Iterable<T>> recursion) {
        return postOrder((Iterable<T>)Collections.singletonList(root), recursion);
    }
    
    public static <T> Iterable<T> postOrder(final Iterable<T> input, final Function<T, Iterable<T>> recursion) {
        return new Iterable<T>() {
            @Override
            public final Iterator<T> iterator() {
                return new Iterator<T>(input) {
                    final Stack<Pair<Iterator<T>, T>> stack;
                    boolean returnedCurrent;
                    T next;
                    
                    {
                        (this.stack = new Stack<Pair<Iterator<T>, T>>()).push(Pair.create(param_1.iterator(), (T)null));
                    }
                    
                    private T selectNext() {
                        if (this.next != null) {
                            return this.next;
                        }
                        while (!this.stack.isEmpty()) {
                            while (this.stack.peek().getFirst().hasNext()) {
                                this.next = this.stack.peek().getFirst().next();
                                if (this.next != null) {
                                    final Iterable<T> children = recursion.apply(this.next);
                                    if (children != null) {
                                        this.stack.push(Pair.create(children.iterator(), this.next));
                                        continue;
                                    }
                                }
                                return this.next;
                            }
                            this.next = this.stack.pop().getSecond();
                            if (this.next != null) {
                                return this.next;
                            }
                        }
                        return null;
                    }
                    
                    @Override
                    public final boolean hasNext() {
                        return this.selectNext() != null;
                    }
                    
                    @Override
                    public final T next() {
                        final T next = this.selectNext();
                        if (next == null) {
                            throw new NoSuchElementException();
                        }
                        this.next = null;
                        return next;
                    }
                    
                    @Override
                    public final void remove() {
                        throw ContractUtils.unsupported();
                    }
                };
            }
        };
    }
}
