package com.strobel.functions;

import com.strobel.util.*;

public final class Suppliers
{
    private Suppliers() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static <T> Supplier<T> forValue(final T value) {
        return new Supplier<T>() {
            @Override
            public T get() {
                return value;
            }
        };
    }
}
