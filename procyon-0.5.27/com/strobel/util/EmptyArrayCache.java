package com.strobel.util;

import com.strobel.collections.*;
import com.strobel.core.*;
import java.lang.reflect.*;

public final class EmptyArrayCache
{
    public static final boolean[] EMPTY_BOOLEAN_ARRAY;
    public static final char[] EMPTY_CHAR_ARRAY;
    public static final byte[] EMPTY_BYTE_ARRAY;
    public static final short[] EMPTY_SHORT_ARRAY;
    public static final int[] EMPTY_INT_ARRAY;
    public static final long[] EMPTY_LONG_ARRAY;
    public static final float[] EMPTY_FLOAT_ARRAY;
    public static final double[] EMPTY_DOUBLE_ARRAY;
    public static final String[] EMPTY_STRING_ARRAY;
    public static final Object[] EMPTY_OBJECT_ARRAY;
    public static final Class<?>[] EMPTY_CLASS_ARRAY;
    private static final Cache<Class<?>, Object> GLOBAL_CACHE;
    private static final Cache<Class<?>, Object> THREAD_LOCAL_CACHE;
    
    static {
        EMPTY_BOOLEAN_ARRAY = new boolean[0];
        EMPTY_CHAR_ARRAY = new char[0];
        EMPTY_BYTE_ARRAY = new byte[0];
        EMPTY_SHORT_ARRAY = new short[0];
        EMPTY_INT_ARRAY = new int[0];
        EMPTY_LONG_ARRAY = new long[0];
        EMPTY_FLOAT_ARRAY = new float[0];
        EMPTY_DOUBLE_ARRAY = new double[0];
        EMPTY_STRING_ARRAY = new String[0];
        EMPTY_OBJECT_ARRAY = new Object[0];
        EMPTY_CLASS_ARRAY = new Class[0];
        GLOBAL_CACHE = Cache.createTopLevelCache();
        (THREAD_LOCAL_CACHE = Cache.createThreadLocalCache(EmptyArrayCache.GLOBAL_CACHE)).cache(Boolean.TYPE, EmptyArrayCache.EMPTY_BOOLEAN_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Character.TYPE, EmptyArrayCache.EMPTY_CHAR_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Byte.TYPE, EmptyArrayCache.EMPTY_BYTE_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Short.TYPE, EmptyArrayCache.EMPTY_SHORT_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Integer.TYPE, EmptyArrayCache.EMPTY_INT_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Long.TYPE, EmptyArrayCache.EMPTY_LONG_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Float.TYPE, EmptyArrayCache.EMPTY_FLOAT_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Double.TYPE, EmptyArrayCache.EMPTY_DOUBLE_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(String.class, EmptyArrayCache.EMPTY_STRING_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Object.class, EmptyArrayCache.EMPTY_OBJECT_ARRAY);
        EmptyArrayCache.THREAD_LOCAL_CACHE.cache(Class.class, EmptyArrayCache.EMPTY_CLASS_ARRAY);
    }
    
    private EmptyArrayCache() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static <T> T[] fromElementType(final Class<T> elementType) {
        VerifyArgument.notNull(elementType, "elementType");
        final Object[] cachedArray = EmptyArrayCache.THREAD_LOCAL_CACHE.get(elementType);
        if (cachedArray != null) {
            return (T[])cachedArray;
        }
        return (T[])EmptyArrayCache.THREAD_LOCAL_CACHE.cache(elementType, Array.newInstance(elementType, 0));
    }
    
    public static Object fromElementOrPrimitiveType(final Class<?> elementType) {
        VerifyArgument.notNull(elementType, "elementType");
        final Object cachedArray = EmptyArrayCache.THREAD_LOCAL_CACHE.get(elementType);
        if (cachedArray != null) {
            return cachedArray;
        }
        return EmptyArrayCache.THREAD_LOCAL_CACHE.cache(elementType, Array.newInstance(elementType, 0));
    }
    
    public static <T> T fromArrayType(final Class<? extends Object[]> arrayType) {
        return (T)(Object)fromElementType(VerifyArgument.notNull(arrayType, "arrayType").getComponentType());
    }
}
