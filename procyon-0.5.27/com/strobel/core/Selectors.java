package com.strobel.core;

import com.strobel.util.*;

public final class Selectors
{
    private static final Selector<?, ?> IDENTITY_SELECTOR;
    private static final Selector<String, String> TO_UPPERCASE;
    private static final Selector<String, String> TO_LOWERCASE;
    private static final Selector<?, String> TO_STRING;
    
    static {
        IDENTITY_SELECTOR = new Selector<Object, Object>() {
            @Override
            public Object select(final Object source) {
                return source;
            }
        };
        TO_UPPERCASE = new Selector<String, String>() {
            @Override
            public String select(final String source) {
                if (source == null) {
                    return null;
                }
                return source.toUpperCase();
            }
        };
        TO_LOWERCASE = new Selector<String, String>() {
            @Override
            public String select(final String source) {
                if (source == null) {
                    return null;
                }
                return source.toUpperCase();
            }
        };
        TO_STRING = new Selector<Object, String>() {
            @Override
            public String select(final Object source) {
                if (source == null) {
                    return null;
                }
                return source.toString();
            }
        };
    }
    
    private Selectors() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static <T> Selector<T, T> identity() {
        return Selectors.IDENTITY_SELECTOR;
    }
    
    public static Selector<String, String> toUpperCase() {
        return Selectors.TO_UPPERCASE;
    }
    
    public static Selector<String, String> toLowerCase() {
        return Selectors.TO_LOWERCASE;
    }
    
    public static <T> Selector<T, String> asString() {
        return Selectors.TO_STRING;
    }
    
    public static <T, R> Selector<T, R> cast(final Class<R> destinationType) {
        return new Selector<T, R>() {
            @Override
            public R select(final T source) {
                return destinationType.cast(source);
            }
        };
    }
    
    public static <T, U, R> Selector<T, R> combine(final Selector<? super T, ? extends U> first, final Selector<? super U, ? extends R> second) {
        VerifyArgument.notNull(first, "first");
        VerifyArgument.notNull(second, "second");
        return new Selector<T, R>() {
            @Override
            public R select(final T source) {
                return second.select(first.select(source));
            }
        };
    }
}
