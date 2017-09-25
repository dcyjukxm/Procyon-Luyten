package com.strobel.core;

public interface Selector<TSource, TResult>
{
    TResult select(TSource param_0);
}
