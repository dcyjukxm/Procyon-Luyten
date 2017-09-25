package com.strobel.core;

import java.lang.reflect.*;
import java.util.*;

public final class Predicates
{
    public static final Predicate<Object> IS_NULL;
    public static final Predicate<Object> NON_NULL;
    public static final Predicate<Object> FALSE;
    public static final Predicate<Object> TRUE;
    
    static {
        IS_NULL = new Predicate<Object>() {
            @Override
            public boolean test(final Object o) {
                return o == null;
            }
        };
        NON_NULL = new Predicate<Object>() {
            @Override
            public boolean test(final Object o) {
                return o != null;
            }
        };
        FALSE = new Predicate<Object>() {
            @Override
            public boolean test(final Object o) {
                return false;
            }
        };
        TRUE = new Predicate<Object>() {
            @Override
            public boolean test(final Object o) {
                return true;
            }
        };
    }
    
    private Predicates() {
        super();
        throw new AssertionError((Object)"No instances!");
    }
    
    public static <T> Predicate<T> isNull() {
        return Predicates.IS_NULL;
    }
    
    public static <T> Predicate<T> nonNull() {
        return Predicates.NON_NULL;
    }
    
    public static <T> Predicate<T> alwaysFalse() {
        return Predicates.FALSE;
    }
    
    public static <T> Predicate<T> alwaysTrue() {
        return Predicates.TRUE;
    }
    
    public static <T> Predicate<T> instanceOf(final Class<?> clazz) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T o) {
                return clazz.isInstance(o);
            }
        };
    }
    
    public static <T> Predicate<T> isSame(final T target) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return t == target;
            }
        };
    }
    
    public static <T> Predicate<T> isEqual(final T target) {
        if (target == null) {
            return isNull();
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return target.equals(t);
            }
        };
    }
    
    public static <T> Predicate<T> contains(final Collection<? extends T> target) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return target.contains(t);
            }
        };
    }
    
    public static <T> Predicate<T> containsKey(final Map<? extends T, ?> target) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return target.containsKey(t);
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> negate(final P predicate) {
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return !((Predicate<T>)predicate).test(t);
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> and(final Predicate<T> first, final P second) {
        if (first != null && first == second) {
            return first;
        }
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return first.test(t) && ((Predicate<T>)second).test(t);
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> and(final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(components);
        if (predicates.isEmpty()) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (!predicate.test((Object)t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
    
    static <T, P extends Predicate<? super T>> Predicate<T> and(final P first, final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(first, components);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (!predicate.test((Object)t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
    
    @SafeVarargs
    public static <T, P extends Predicate<? super T>> Predicate<T> and(final P... components) {
        final Predicate[] predicates = safeCopyOf(components);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Predicate[] loc_1;
                for (int loc_0 = (loc_1 = predicates).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final P predicate = (P)loc_1[loc_2];
                    if (!predicate.test((Object)t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
    
    @SafeVarargs
    static <T, P extends Predicate<? super T>> Predicate<T> and(final P first, final P... components) {
        final Predicate[] predicates = safeCopyOf(first, components);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Predicate[] loc_1;
                for (int loc_0 = (loc_1 = predicates).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final P predicate = (P)loc_1[loc_2];
                    if (!predicate.test((Object)t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> or(final Predicate<T> first, final P second) {
        if (first != null && first == second) {
            return first;
        }
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return first.test(t) || ((Predicate<T>)second).test(t);
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> or(final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(components);
        if (predicates.isEmpty()) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (predicate.test((Object)t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    static <T, P extends Predicate<? super T>> Predicate<T> or(final P first, final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(first, components);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                for (final P predicate : predicates) {
                    if (predicate.test((Object)t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    @SafeVarargs
    public static <T, P extends Predicate<? super T>> Predicate<T> or(final P... components) {
        final Predicate[] predicates = safeCopyOf(components);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Predicate[] loc_1;
                for (int loc_0 = (loc_1 = predicates).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final P predicate = (P)loc_1[loc_2];
                    if (predicate.test((Object)t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    @SafeVarargs
    static <T, P extends Predicate<? super T>> Predicate<T> or(final Predicate<T> first, final P... components) {
        final Predicate[] predicates = safeCopyOf(first, (Predicate<? super T>[])components);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Predicate[] loc_1;
                for (int loc_0 = (loc_1 = predicates).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final P predicate = (P)loc_1[loc_2];
                    if (predicate.test((Object)t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> xor(final Predicate<T> first, final P second) {
        if (first != null && first == second) {
            return alwaysFalse();
        }
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                return first.test(t) ^ ((Predicate<T>)second).test(t);
            }
        };
    }
    
    public static <T, P extends Predicate<? super T>> Predicate<T> xor(final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(components);
        if (predicates.isEmpty()) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;
                for (final P predicate : predicates) {
                    if (initial == null) {
                        initial = predicate.test((Object)t);
                    }
                    else {
                        if (!(initial ^ predicate.test((Object)t))) {
                            return true;
                        }
                        continue;
                    }
                }
                return false;
            }
        };
    }
    
    @SafeVarargs
    public static <T, P extends Predicate<? super T>> Predicate<T> xor(final P... components) {
        final Predicate[] predicates = safeCopyOf(components);
        if (predicates.length == 0) {
            throw new IllegalArgumentException("no predicates");
        }
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;
                Predicate[] loc_1;
                for (int loc_0 = (loc_1 = predicates).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final P predicate = (P)loc_1[loc_2];
                    if (initial == null) {
                        initial = predicate.test((Object)t);
                    }
                    else if (!(initial ^ predicate.test((Object)t))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    @SafeVarargs
    static <T, P extends Predicate<? super T>> Predicate<T> xor(final Predicate<T> first, final P... components) {
        final Predicate[] predicates = safeCopyOf(first, (Predicate<? super T>[])components);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;
                Predicate[] loc_1;
                for (int loc_0 = (loc_1 = predicates).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
                    final P predicate = (P)loc_1[loc_2];
                    if (initial == null) {
                        initial = predicate.test((Object)t);
                    }
                    else if (!(initial ^ predicate.test((Object)t))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
    
    static <T, P extends Predicate<? super T>> Predicate<T> xor(final Predicate<T> first, final Iterable<P> components) {
        final List<P> predicates = safeCopyOf(first, components);
        return new Predicate<T>() {
            @Override
            public boolean test(final T t) {
                Boolean initial = null;
                for (final P predicate : predicates) {
                    if (initial == null) {
                        initial = predicate.test((Object)t);
                    }
                    else {
                        if (!(initial ^ predicate.test((Object)t))) {
                            return true;
                        }
                        continue;
                    }
                }
                return false;
            }
        };
    }
    
    @SafeVarargs
    private static <T> T[] safeCopyOf(final T... array) {
        final Object[] copy = Arrays.copyOf(array, array.length);
        Object[] loc_1;
        for (int loc_0 = (loc_1 = copy).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final T each = (T)loc_1[loc_2];
            Objects.requireNonNull(each);
        }
        return (T[])copy;
    }
    
    @SafeVarargs
    private static <T> T[] safeCopyOf(final T first, final T... array) {
        final Object[] copy = (Object[])Array.newInstance(array.getClass().getComponentType(), array.length + 1);
        copy[0] = Objects.requireNonNull(first);
        System.arraycopy(array, 0, copy, 1, array.length);
        Object[] loc_1;
        for (int loc_0 = (loc_1 = copy).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final T each = (T)loc_1[loc_2];
            Objects.requireNonNull(each);
        }
        return (T[])copy;
    }
    
    private static <T> List<T> safeCopyOf(final T first, final Iterable<T> iterable) {
        final ArrayList<T> list = new ArrayList<T>();
        list.add(Objects.requireNonNull(first));
        for (final T element : iterable) {
            list.add(Objects.requireNonNull(element));
        }
        return list;
    }
    
    private static <T> List<T> safeCopyOf(final Iterable<T> iterable) {
        final ArrayList<T> list = new ArrayList<T>();
        for (final T element : iterable) {
            list.add(Objects.requireNonNull(element));
        }
        return list;
    }
}
