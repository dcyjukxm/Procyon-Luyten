package com.strobel.core;

import com.strobel.annotations.*;
import java.util.*;

public interface IReadOnlyList<T> extends Iterable<T>, RandomAccess
{
    int size();
    
     <U extends T> int indexOf(U param_0);
    
     <U extends T> int lastIndexOf(U param_0);
    
    boolean isEmpty();
    
     <U extends T> boolean contains(U param_0);
    
    boolean containsAll(Iterable<? extends T> param_0);
    
    T get(int param_0);
    
    @NotNull
    T[] toArray();
    
    @NotNull
     <T> T[] toArray(T[] param_0);
    
    @NotNull
    ListIterator<T> listIterator();
    
    @NotNull
    ListIterator<T> listIterator(int param_0);
}
