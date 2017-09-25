package com.strobel.core;

import java.util.*;

public final class VerifyArgument
{
    public static <T> T notNull(final T value, final String parameterName) {
        if (value != null) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' cannot be null.", parameterName));
    }
    
    public static <T> T[] notEmpty(final T[] array, final String parameterName) {
        notNull(array, parameterName);
        if (array.length == 0) {
            throw new IllegalArgumentException(String.format("Argument '%s' must be a non-empty collection.", parameterName));
        }
        return array;
    }
    
    public static <T extends Iterable<?>> T notEmpty(final T collection, final String parameterName) {
        notNull(collection, parameterName);
        if (collection instanceof Collection) {
            if (!((Collection)collection).isEmpty()) {
                return collection;
            }
        }
        else {
            final Iterator<?> iterator = collection.iterator();
            if (iterator.hasNext()) {
                return collection;
            }
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be a non-empty collection.", parameterName));
    }
    
    public static <T> T[] noNullElements(final T[] array, final String parameterName) {
        notNull(array, parameterName);
        for (final T item : array) {
            if (item == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements.", parameterName));
            }
        }
        return array;
    }
    
    public static <T> T[] noNullElements(final T[] array, final int offset, final int length, final String parameterName) {
        notNull(array, parameterName);
        for (int i = offset, end = offset + length; i < end; ++i) {
            final T item = array[i];
            if (item == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements in the range (%s, %s].", parameterName, offset, offset + length));
            }
        }
        return array;
    }
    
    public static <T extends Iterable<?>> T noNullElements(final T collection, final String parameterName) {
        notNull(collection, parameterName);
        if (collection instanceof List && collection instanceof RandomAccess) {
            final List<?> list = (List<?>)collection;
            for (int i = 0, n = list.size(); i < n; ++i) {
                if (list.get(i) == null) {
                    throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements.", parameterName));
                }
            }
            return collection;
        }
        for (final Object item : collection) {
            if (item == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements.", parameterName));
            }
        }
        return collection;
    }
    
    public static <T> T[] noNullElementsAndNotEmpty(final T[] array, final String parameterName) {
        notEmpty((Object[])array, parameterName);
        for (final T item : array) {
            if (item == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements.", parameterName));
            }
        }
        return array;
    }
    
    public static <T> T[] noNullElementsAndNotEmpty(final T[] array, final int offset, final int length, final String parameterName) {
        notEmpty(array, parameterName);
        for (int i = offset, end = offset + length; i < end; ++i) {
            final T item = array[i];
            if (item == null) {
                throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements in the range (%s, %s].", parameterName, offset, offset + length));
            }
        }
        return array;
    }
    
    public static <T extends Iterable<?>> T noNullElementsAndNotEmpty(final T collection, final String parameterName) {
        notNull(collection, parameterName);
        if (collection instanceof List && collection instanceof RandomAccess) {
            final List<?> list = (List<?>)collection;
            if (list.isEmpty()) {
                throw new IllegalArgumentException(String.format("Argument '%s' must be a non-empty collection.", parameterName));
            }
            for (int i = 0, n = list.size(); i < n; ++i) {
                if (list.get(i) == null) {
                    throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements.", parameterName));
                }
            }
            return collection;
        }
        else {
            final Iterator iterator = collection.iterator();
            if (!iterator.hasNext()) {
                throw new IllegalArgumentException(String.format("Argument '%s' must be a non-empty collection.", parameterName));
            }
            do {
                final Object item = iterator.next();
                if (item == null) {
                    throw new IllegalArgumentException(String.format("Argument '%s' must not have any null elements.", parameterName));
                }
            } while (iterator.hasNext());
            return collection;
        }
    }
    
    public static <T> T[] elementsOfType(final Class<?> elementType, final T[] values, final String parameterName) {
        notNull(elementType, "elementType");
        notNull(values, "values");
        for (final T value : values) {
            if (!elementType.isInstance(value)) {
                throw new IllegalArgumentException(String.format("Argument '%s' must only contain elements of type '%s'.", parameterName, elementType));
            }
        }
        return values;
    }
    
    public static <T> T[] elementsOfTypeOrNull(final Class<T> elementType, final T[] values, final String parameterName) {
        notNull(elementType, "elementType");
        notNull(values, "values");
        for (final T value : values) {
            if (value != null && !elementType.isInstance(value)) {
                throw new IllegalArgumentException(String.format("Argument '%s' must only contain elements of type '%s'.", parameterName, elementType));
            }
        }
        return values;
    }
    
    public static int validElementRange(final int size, final int startInclusive, final int endExclusive) {
        if (startInclusive >= 0 && endExclusive <= size && endExclusive >= startInclusive) {
            return endExclusive - startInclusive;
        }
        throw new IllegalArgumentException(String.format("The specified element range is not valid: range=(%d, %d], length=%d", startInclusive, endExclusive, size));
    }
    
    public static String notNullOrEmpty(final String value, final String parameterName) {
        if (!StringUtilities.isNullOrEmpty(value)) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be a non-null, non-empty string.", parameterName));
    }
    
    public static String notNullOrWhitespace(final String value, final String parameterName) {
        if (!StringUtilities.isNullOrWhitespace(value)) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be a non-null, non-empty string.", parameterName));
    }
    
    public static int isNonZero(final int value, final String parameterName) {
        if (value != 0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be non-zero, but value was: %d.", parameterName, value));
    }
    
    public static int isPositive(final int value, final String parameterName) {
        if (value > 0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be positive, but value was: %d.", parameterName, value));
    }
    
    public static int isNonNegative(final int value, final String parameterName) {
        if (value >= 0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be non-negative, but value was: %d.", parameterName, value));
    }
    
    public static int isNegative(final int value, final String parameterName) {
        if (value < 0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be negative, but value was: %d.", parameterName, value));
    }
    
    public static int inRange(final int minInclusive, final int maxInclusive, final int value, final String parameterName) {
        if (maxInclusive < minInclusive) {
            throw new IllegalArgumentException(String.format("The specified maximum value (%d) is less than the specified minimum value (%d).", maxInclusive, minInclusive));
        }
        if (value >= minInclusive && value <= maxInclusive) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be in the range [%s, %s], but value was: %d.", parameterName, minInclusive, maxInclusive, value));
    }
    
    public static double isNonZero(final double value, final String parameterName) {
        if (value != 0.0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be non-zero, but value was: %s.", parameterName, value));
    }
    
    public static double isPositive(final double value, final String parameterName) {
        if (value > 0.0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be positive, but value was: %s.", parameterName, value));
    }
    
    public static double isNonNegative(final double value, final String parameterName) {
        if (value >= 0.0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be non-negative, but value was: %s.", parameterName, value));
    }
    
    public static double isNegative(final double value, final String parameterName) {
        if (value < 0.0) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be negative, but value was: %s.", parameterName, value));
    }
    
    public static double inRange(final double minInclusive, final double maxInclusive, final double value, final String parameterName) {
        if (Double.isNaN(minInclusive)) {
            throw new IllegalArgumentException("The minimum value cannot be NaN.");
        }
        if (Double.isNaN(maxInclusive)) {
            throw new IllegalArgumentException("The maximum value cannot be NaN.");
        }
        if (maxInclusive < minInclusive) {
            throw new IllegalArgumentException(String.format("The specified maximum value (%s) is less than the specified minimum value (%s).", maxInclusive, minInclusive));
        }
        if (value >= minInclusive && value <= maxInclusive) {
            return value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be in the range [%s, %s], but value was: %s.", parameterName, minInclusive, maxInclusive, value));
    }
    
    public static <T> T instanceOf(final Class<T> type, final Object value, final String parameterName) {
        final Class<?> actualType = getBoxedType(notNull(type, "type"));
        if (actualType.isInstance(value)) {
            return (T)value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must be an instance of type %s.", parameterName, type.getCanonicalName()));
    }
    
    public static <T> T notInstanceOf(final Class<T> type, final Object value, final String parameterName) {
        final Class<?> actualType = getBoxedType(notNull(type, "type"));
        if (!actualType.isInstance(value)) {
            return (T)value;
        }
        throw new IllegalArgumentException(String.format("Argument '%s' must not be an instance of type %s.", parameterName, type.getCanonicalName()));
    }
    
    private static Class<?> getBoxedType(final Class<?> type) {
        if (!type.isPrimitive()) {
            return type;
        }
        if (type == Boolean.TYPE) {
            return Boolean.class;
        }
        if (type == Character.TYPE) {
            return Character.class;
        }
        if (type == Byte.TYPE) {
            return Byte.class;
        }
        if (type == Short.TYPE) {
            return Short.class;
        }
        if (type == Integer.TYPE) {
            return Integer.class;
        }
        if (type == Long.TYPE) {
            return Long.class;
        }
        if (type == Float.TYPE) {
            return Float.class;
        }
        if (type == Double.TYPE) {
            return Double.class;
        }
        return type;
    }
}
