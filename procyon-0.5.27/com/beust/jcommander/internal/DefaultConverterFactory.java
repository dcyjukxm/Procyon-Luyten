package com.beust.jcommander.internal;

import com.beust.jcommander.*;
import java.io.*;
import java.math.*;
import java.util.*;
import com.beust.jcommander.converters.*;

public class DefaultConverterFactory implements IStringConverterFactory
{
    private static Map<Class, Class<? extends IStringConverter<?>>> m_classConverters;
    
    public Class<? extends IStringConverter<?>> getConverter(final Class forType) {
        return DefaultConverterFactory.m_classConverters.get(forType);
    }
    
    static {
        (DefaultConverterFactory.m_classConverters = Maps.newHashMap()).put(String.class, StringConverter.class);
        DefaultConverterFactory.m_classConverters.put(Integer.class, IntegerConverter.class);
        DefaultConverterFactory.m_classConverters.put(Integer.TYPE, IntegerConverter.class);
        DefaultConverterFactory.m_classConverters.put(Long.class, LongConverter.class);
        DefaultConverterFactory.m_classConverters.put(Long.TYPE, LongConverter.class);
        DefaultConverterFactory.m_classConverters.put(Float.class, FloatConverter.class);
        DefaultConverterFactory.m_classConverters.put(Float.TYPE, FloatConverter.class);
        DefaultConverterFactory.m_classConverters.put(Double.class, DoubleConverter.class);
        DefaultConverterFactory.m_classConverters.put(Double.TYPE, DoubleConverter.class);
        DefaultConverterFactory.m_classConverters.put(Boolean.class, BooleanConverter.class);
        DefaultConverterFactory.m_classConverters.put(Boolean.TYPE, BooleanConverter.class);
        DefaultConverterFactory.m_classConverters.put(File.class, FileConverter.class);
        DefaultConverterFactory.m_classConverters.put(BigDecimal.class, BigDecimalConverter.class);
        DefaultConverterFactory.m_classConverters.put(Date.class, ISO8601DateConverter.class);
    }
}
