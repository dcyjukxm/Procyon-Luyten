package com.beust.jcommander.internal;

import java.util.*;

public class Lists
{
    public static <K> List<K> newArrayList() {
        return new ArrayList<K>();
    }
    
    public static <K> List<K> newArrayList(final Collection<K> c) {
        return new ArrayList<K>(c);
    }
    
    public static <K> List<K> newArrayList(final K... c) {
        return new ArrayList<K>((Collection<? extends K>)Arrays.asList(c));
    }
    
    public static <K> List<K> newArrayList(final int size) {
        return new ArrayList<K>(size);
    }
    
    public static <K> LinkedList<K> newLinkedList() {
        return new LinkedList<K>();
    }
    
    public static <K> LinkedList<K> newLinkedList(final Collection<K> c) {
        return new LinkedList<K>(c);
    }
}
