package com.strobel.core;

import java.util.*;

public final class HashUtilities
{
    public static final int NullHashCode = 1642088727;
    private static final int HashPrime = 101;
    private static final int CombinedHashOffset = 5;
    private static final int MaxPrimeArrayLength = 2146435069;
    private static final int[] Primes;
    
    static {
        Primes = new int[] { 3, 7, 11, 17, 23, 29, 37, 47, 59, 71, 89, 107, 131, 163, 197, 239, 293, 353, 431, 521, 631, 761, 919, 1103, 1327, 1597, 1931, 2333, 2801, 3371, 4049, 4861, 5839, 7013, 8419, 10103, 12143, 14591, 17519, 21023, 25229, 30293, 36353, 43627, 52361, 62851, 75431, 90523, 108631, 130363, 156437, 187751, 225307, 270371, 324449, 389357, 467237, 560689, 672827, 807403, 968897, 1162687, 1395263, 1674319, 2009191, 2411033, 2893249, 3471899, 4166287, 4999559, 5999471, 7199369 };
    }
    
    private HashUtilities() {
        super();
        throw new UnsupportedOperationException();
    }
    
    public static int hashCode(final Object o) {
        if (o == null) {
            return 1642088727;
        }
        if (o.getClass().isArray()) {
            if (o instanceof Object[]) {
                return combineHashCodes((Object[])o);
            }
            if (o instanceof byte[]) {
                return Arrays.hashCode((byte[])o);
            }
            if (o instanceof short[]) {
                return Arrays.hashCode((short[])o);
            }
            if (o instanceof int[]) {
                return Arrays.hashCode((int[])o);
            }
            if (o instanceof long[]) {
                return Arrays.hashCode((long[])o);
            }
            if (o instanceof char[]) {
                return Arrays.hashCode((char[])o);
            }
            if (o instanceof float[]) {
                return Arrays.hashCode((float[])o);
            }
            if (o instanceof double[]) {
                return Arrays.hashCode((double[])o);
            }
            if (o instanceof boolean[]) {
                return Arrays.hashCode((boolean[])o);
            }
        }
        return o.hashCode();
    }
    
    public static int combineHashCodes(final int... hashes) {
        int hash = 0;
        for (final int h : hashes) {
            hash <<= 5;
            hash ^= h;
        }
        return hash;
    }
    
    public static int combineHashCodes(final Object... objects) {
        int hash = 0;
        for (final Object o : objects) {
            int entryHash = 1642088727;
            if (o != null) {
                if (o instanceof Object[]) {
                    entryHash = combineHashCodes((Object[])o);
                }
                else {
                    entryHash = hashCode(o);
                }
            }
            hash <<= 5;
            hash ^= entryHash;
        }
        return hash;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2) {
        return hash1 << 5 ^ hash2;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2, final int hash3) {
        return (hash1 << 5 ^ hash2) << 5 ^ hash3;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2, final int hash3, final int hash4) {
        return ((hash1 << 5 ^ hash2) << 5 ^ hash3) << 5 ^ hash4;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2, final int hash3, final int hash4, final int hash5) {
        return (((hash1 << 5 ^ hash2) << 5 ^ hash3) << 5 ^ hash4) << 5 ^ hash5;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2, final int hash3, final int hash4, final int hash5, final int hash6) {
        return ((((hash1 << 5 ^ hash2) << 5 ^ hash3) << 5 ^ hash4) << 5 ^ hash5) << 5 ^ hash6;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2, final int hash3, final int hash4, final int hash5, final int hash6, final int hash7) {
        return (((((hash1 << 5 ^ hash2) << 5 ^ hash3) << 5 ^ hash4) << 5 ^ hash5) << 5 ^ hash6) << 5 ^ hash7;
    }
    
    public static int combineHashCodes(final int hash1, final int hash2, final int hash3, final int hash4, final int hash5, final int hash6, final int hash7, final int hash8) {
        return ((((((hash1 << 5 ^ hash2) << 5 ^ hash3) << 5 ^ hash4) << 5 ^ hash5) << 5 ^ hash6) << 5 ^ hash7) << 5 ^ hash8;
    }
    
    public static int combineHashCodes(final Object o1, final Object o2) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2));
    }
    
    public static int combineHashCodes(final Object o1, final Object o2, final Object o3) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2), (o3 == null) ? 1642088727 : hashCode(o3));
    }
    
    public static int combineHashCodes(final Object o1, final Object o2, final Object o3, final Object o4) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2), (o3 == null) ? 1642088727 : hashCode(o3), (o4 == null) ? 1642088727 : hashCode(o4));
    }
    
    public static int combineHashCodes(final Object o1, final Object o2, final Object o3, final Object o4, final Object o5) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2), (o3 == null) ? 1642088727 : hashCode(o3), (o4 == null) ? 1642088727 : hashCode(o4), (o5 == null) ? 1642088727 : hashCode(o5));
    }
    
    public static int combineHashCodes(final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2), (o3 == null) ? 1642088727 : hashCode(o3), (o4 == null) ? 1642088727 : hashCode(o4), (o5 == null) ? 1642088727 : hashCode(o5), (o6 == null) ? 1642088727 : hashCode(o6));
    }
    
    public static int combineHashCodes(final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6, final Object o7) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2), (o3 == null) ? 1642088727 : hashCode(o3), (o4 == null) ? 1642088727 : hashCode(o4), (o5 == null) ? 1642088727 : hashCode(o5), (o6 == null) ? 1642088727 : hashCode(o6), (o7 == null) ? 1642088727 : hashCode(o7));
    }
    
    public static int combineHashCodes(final Object o1, final Object o2, final Object o3, final Object o4, final Object o5, final Object o6, final Object o7, final Object o8) {
        return combineHashCodes((o1 == null) ? 1642088727 : hashCode(o1), (o2 == null) ? 1642088727 : hashCode(o2), (o3 == null) ? 1642088727 : hashCode(o3), (o4 == null) ? 1642088727 : hashCode(o4), (o5 == null) ? 1642088727 : hashCode(o5), (o6 == null) ? 1642088727 : hashCode(o6), (o7 == null) ? 1642088727 : hashCode(o7), (o8 == null) ? 1642088727 : hashCode(o8));
    }
    
    public static boolean isPrime(final int candidate) {
        if ((candidate & 0x1) != 0x0) {
            for (int limit = (int)Math.sqrt(candidate), divisor = 3; divisor <= limit; divisor += 2) {
                if (candidate % divisor == 0) {
                    return false;
                }
            }
            return true;
        }
        return candidate == 2;
    }
    
    public static int getPrime(final int min) {
        VerifyArgument.isNonNegative(min, "min");
        int[] loc_1;
        for (int loc_0 = (loc_1 = HashUtilities.Primes).length, loc_2 = 0; loc_2 < loc_0; ++loc_2) {
            final int prime = loc_1[loc_2];
            if (prime >= min) {
                return prime;
            }
        }
        for (int i = min | 0x1; i < Integer.MAX_VALUE; i += 2) {
            if (isPrime(i) && (i - 1) % 101 != 0) {
                return i;
            }
        }
        return min;
    }
    
    public static int getMinPrime() {
        return HashUtilities.Primes[0];
    }
    
    public static int expandPrime(final int oldSize) {
        final int newSize = 2 * oldSize;
        if (Math.abs(newSize) <= 2146435069 || 2146435069 <= oldSize) {
            return getPrime(newSize);
        }
        assert 2146435069 == getPrime(2146435069) : "Invalid MaxPrimeArrayLength";
        return 2146435069;
    }
}
