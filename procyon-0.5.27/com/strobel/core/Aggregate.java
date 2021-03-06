package com.strobel.core;

import com.strobel.util.*;
import java.util.*;

public final class Aggregate
{
    private Aggregate() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static <TSource, TAccumulate> TAccumulate aggregate(final Iterable<TSource> source, final Accumulator<TSource, TAccumulate> accumulator) {
        return aggregate(source, (TAccumulate)null, accumulator);
    }
    
    public static <TSource, TAccumulate> TAccumulate aggregate(final Iterable<TSource> source, final TAccumulate seed, final Accumulator<TSource, TAccumulate> accumulator) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(accumulator, "accumulator");
        TAccumulate accumulate = seed;
        for (final TSource item : source) {
            accumulate = accumulator.accumulate(accumulate, item);
        }
        return accumulate;
    }
    
    public static <TSource, TAccumulate, TResult> TResult aggregate(final Iterable<TSource> source, final Accumulator<TSource, TAccumulate> accumulator, final Selector<TAccumulate, TResult> resultSelector) {
        return aggregate(source, (TAccumulate)null, accumulator, resultSelector);
    }
    
    public static <TSource, TAccumulate, TResult> TResult aggregate(final Iterable<TSource> source, final TAccumulate seed, final Accumulator<TSource, TAccumulate> accumulator, final Selector<TAccumulate, TResult> resultSelector) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(accumulator, "accumulator");
        VerifyArgument.notNull(resultSelector, "resultSelector");
        TAccumulate accumulate = seed;
        for (final TSource item : source) {
            accumulate = accumulator.accumulate(accumulate, item);
        }
        return resultSelector.select(accumulate);
    }
}
