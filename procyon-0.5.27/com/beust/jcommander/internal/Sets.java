package com.beust.jcommander.internal;

import java.util.*;

public class Sets
{
    public static <K> Set<K> newHashSet() {
        return new HashSet<K>();
    }
    
    public static <K> Set<K> newLinkedHashSet() {
        return new LinkedHashSet<K>();
    }
}
