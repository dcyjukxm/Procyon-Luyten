package com.strobel.assembler.metadata;

import com.strobel.util.*;
import com.strobel.core.*;

public final class MetadataFilters
{
    private MetadataFilters() {
        super();
        throw ContractUtils.unreachable();
    }
    
    public static <T extends TypeReference> Predicate<T> isSubType(final TypeReference anchor) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isSubType(t, anchor);
            }
        };
    }
    
    public static <T extends TypeReference> Predicate<T> isSuperType(final TypeReference anchor) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isSubType(anchor, t);
            }
        };
    }
    
    public static <T extends TypeReference> Predicate<T> isAssignableFrom(final TypeReference sourceType) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isAssignableFrom(t, sourceType);
            }
        };
    }
    
    public static <T extends TypeReference> Predicate<T> isAssignableTo(final TypeReference targetType) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return MetadataHelper.isAssignableFrom(targetType, t);
            }
        };
    }
    
    public static <T extends MemberReference> Predicate<T> matchName(final String name) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return StringUtilities.equals(t.getName(), name);
            }
        };
    }
    
    public static <T extends MemberReference> Predicate<T> matchDescriptor(final String descriptor) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return StringUtilities.equals(t.getErasedSignature(), descriptor);
            }
        };
    }
    
    public static <T extends MemberReference> Predicate<T> matchSignature(final String signature) {
        return new Predicate<T>() {
            @Override
            public final boolean test(final T t) {
                return StringUtilities.equals(t.getSignature(), signature);
            }
        };
    }
    
    public static <T extends MemberReference> Predicate<T> matchNameAndDescriptor(final String name, final String descriptor) {
        return Predicates.and(matchName(name), matchDescriptor(descriptor));
    }
    
    public static <T extends MemberReference> Predicate<T> matchNameAndSignature(final String name, final String signature) {
        return Predicates.and(matchName(name), matchSignature(signature));
    }
}
