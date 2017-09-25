package com.strobel.core;

public interface Aggregator<TSource, TAccumulate, TResult>
{
    TResult aggregate(TSource param_0, TAccumulate param_1, Accumulator<TSource, TAccumulate> param_2, Selector<TAccumulate, TResult> param_3);
}
