package com.strobel.compilerservices;

import sun.misc.*;
import com.strobel.util.*;
import com.strobel.core.*;
import java.lang.reflect.*;

public final class RuntimeHelpers
{
    private static Unsafe _unsafe;
    
    private RuntimeHelpers() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static void ensureClassInitialized(final Class<?> clazz) {
        getUnsafeInstance().ensureClassInitialized(VerifyArgument.notNull(clazz, "clazz"));
    }
    
    private static Unsafe getUnsafeInstance() {
        if (RuntimeHelpers._unsafe != null) {
            return RuntimeHelpers._unsafe;
        }
        try {
            RuntimeHelpers._unsafe = Unsafe.getUnsafe();
        }
        catch (Throwable loc_0) {}
        try {
            final Field instanceField = Unsafe.class.getDeclaredField("theUnsafe");
            instanceField.setAccessible(true);
            RuntimeHelpers._unsafe = (Unsafe)instanceField.get(Unsafe.class);
        }
        catch (Throwable t) {
            throw new IllegalStateException(String.format("Could not load an instance of the %s class.", Unsafe.class.getName()));
        }
        return RuntimeHelpers._unsafe;
    }
}
