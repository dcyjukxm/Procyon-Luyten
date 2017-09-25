package com.strobel.core;

import java.util.*;

public abstract class StringComparator implements Comparator<String>, IEqualityComparator<String>
{
    public static final StringComparator Ordinal;
    public static final StringComparator OrdinalIgnoreCase;
    
    static {
        Ordinal = new StringComparator() {
            @Override
            public int compare(final String s1, final String s2) {
                if (s1 != null) {
                    return s1.compareTo(s2);
                }
                if (s2 == null) {
                    return 0;
                }
                return -1;
            }
            
            @Override
            public boolean equals(final String s1, final String s2) {
                return (s1 == null) ? (s2 == null) : s1.equals(s2);
            }
            
            @Override
            public int hash(final String s) {
                if (s == null) {
                    return 0;
                }
                return s.hashCode();
            }
        };
        OrdinalIgnoreCase = new StringComparator() {
            @Override
            public int compare(final String s1, final String s2) {
                return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            }
            
            @Override
            public boolean equals(final String s1, final String s2) {
                return (s1 == null) ? (s2 == null) : s1.equalsIgnoreCase(s2);
            }
            
            @Override
            public int hash(final String s) {
                return StringUtilities.getHashCodeIgnoreCase(s);
            }
        };
    }
}
