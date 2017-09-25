package com.strobel.core;

public interface Accumulator<TSource, TAccumulate>
{
    TAccumulate accumulate(TAccumulate param_0, TSource param_1);
}
