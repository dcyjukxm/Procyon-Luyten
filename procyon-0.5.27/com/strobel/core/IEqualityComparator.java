package com.strobel.core;

public interface IEqualityComparator<T>
{
    boolean equals(T param_0, T param_1);
    
    int hash(T param_0);
}
