package com.strobel.collections;

import java.util.*;
import com.strobel.core.*;
import com.strobel.util.*;

public final class ArrayIterator<E> implements Iterator<E>
{
    private final E[] _elements;
    private int _index;
    
    public ArrayIterator(final E[] elements) {
        super();
        this._elements = VerifyArgument.notNull(elements, "elements");
    }
    
    @Override
    public boolean hasNext() {
        return this._index < this._elements.length;
    }
    
    @Override
    public E next() {
        return (E)this._elements[this._index++];
    }
    
    @Override
    public void remove() {
        throw ContractUtils.unsupported();
    }
}
