package com.beust.jcommander;

import java.util.*;
import com.beust.jcommander.internal.*;

public class FuzzyMap
{
    public static <V> V findInMap(final Map<? extends IKey, V> map, final IKey name, final boolean caseSensitive, final boolean allowAbbreviations) {
        if (allowAbbreviations) {
            return findAbbreviatedValue(map, name, caseSensitive);
        }
        if (caseSensitive) {
            return map.get(name);
        }
        for (final IKey c : map.keySet()) {
            if (c.getName().equalsIgnoreCase(name.getName())) {
                return map.get(c);
            }
        }
        return null;
    }
    
    private static <V> V findAbbreviatedValue(final Map<? extends IKey, V> map, final IKey name, final boolean caseSensitive) {
        final String string = name.getName();
        final Map<String, V> results = Maps.newHashMap();
        for (final IKey c : map.keySet()) {
            final String n = c.getName();
            final boolean match = (caseSensitive && n.startsWith(string)) || (!caseSensitive && n.toLowerCase().startsWith(string.toLowerCase()));
            if (match) {
                results.put(n, map.get(c));
            }
        }
        if (results.size() > 1) {
            throw new ParameterException("Ambiguous option: " + name + " matches " + results.keySet());
        }
        V result;
        if (results.size() == 1) {
            result = results.values().iterator().next();
        }
        else {
            result = null;
        }
        return result;
    }
    
    interface IKey
    {
        String getName();
    }
}
