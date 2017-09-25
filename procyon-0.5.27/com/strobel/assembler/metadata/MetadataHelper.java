package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;
import com.strobel.collections.*;

public final class MetadataHelper
{
    private static final ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>> CONTAINS_TYPE_CACHE;
    private static final ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>> ADAPT_CACHE;
    private static final TypeMapper<Void> UPPER_BOUND_VISITOR;
    private static final TypeMapper<Void> LOWER_BOUND_VISITOR;
    private static final TypeRelation IS_SUBTYPE_VISITOR;
    private static final TypeRelation CONTAINS_TYPE_VISITOR;
    private static final TypeMapper<TypeReference> AS_SUPER_VISITOR;
    private static final TypeMapper<Void> SUPER_VISITOR;
    private static final SameTypeVisitor SAME_TYPE_VISITOR_LOOSE;
    private static final SameTypeVisitor SAME_TYPE_VISITOR_STRICT;
    private static final DefaultTypeVisitor<Void, List<TypeReference>> INTERFACES_VISITOR;
    private static final TypeMapper<TypeReference> AS_SUBTYPE_VISITOR;
    private static final DefaultTypeVisitor<Boolean, TypeReference> ERASE_VISITOR;
    private static final DefaultTypeVisitor<Void, Boolean> IS_DECLARED_TYPE;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    static {
        CONTAINS_TYPE_CACHE = new ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>>() {
            @Override
            protected final HashSet<Pair<TypeReference, TypeReference>> initialValue() {
                return new HashSet<Pair<TypeReference, TypeReference>>();
            }
        };
        ADAPT_CACHE = new ThreadLocal<HashSet<Pair<TypeReference, TypeReference>>>() {
            @Override
            protected final HashSet<Pair<TypeReference, TypeReference>> initialValue() {
                return new HashSet<Pair<TypeReference, TypeReference>>();
            }
        };
        UPPER_BOUND_VISITOR = new TypeMapper<Void>() {
            @Override
            public TypeReference visitType(final TypeReference t, final Void ignored) {
                if (t.isWildcardType() || t.isGenericParameter() || t instanceof ICapturedType) {
                    return (t.isUnbounded() || t.hasSuperBound()) ? BuiltinTypes.Object : ((DefaultTypeVisitor<P, TypeReference>)this).visit(t.getExtendsBound());
                }
                return t;
            }
            
            public TypeReference visitCapturedType(final CapturedType t, final Void ignored) {
                return t.getExtendsBound();
            }
        };
        LOWER_BOUND_VISITOR = new TypeMapper<Void>() {
            public TypeReference visitWildcard(final WildcardType t, final Void ignored) {
                return t.hasSuperBound() ? ((DefaultTypeVisitor<P, TypeReference>)this).visit(t.getSuperBound()) : BuiltinTypes.Bottom;
            }
            
            public TypeReference visitCapturedType(final CapturedType t, final Void ignored) {
                return t.getSuperBound();
            }
        };
        IS_SUBTYPE_VISITOR = new TypeRelation() {
            private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
            
            @Override
            public Boolean visitArrayType(final ArrayType t, final TypeReference s) {
                if (s.isArray()) {
                    final TypeReference et = MetadataHelper.getElementType(t);
                    final TypeReference es = MetadataHelper.getElementType(s);
                    if (et.isPrimitive()) {
                        return MetadataHelper.isSameType(et, es);
                    }
                    return MetadataHelper.isSubTypeNoCapture(et, es);
                }
                else {
                    final String sName = s.getInternalName();
                    if (!StringUtilities.equals(sName, "java/lang/Object") && !StringUtilities.equals(sName, "java/lang/Cloneable") && !StringUtilities.equals(sName, "java/io/Serializable")) {
                        return false;
                    }
                    return true;
                }
            }
            
            @Override
            public Boolean visitBottomType(final TypeReference t, final TypeReference s) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[t.getSimpleType().ordinal()]) {
                    case 9:
                    case 10:
                    case 11: {
                        return true;
                    }
                    default: {
                        return false;
                    }
                }
            }
            
            @Override
            public Boolean visitClassType(final TypeReference t, final TypeReference s) {
                final TypeReference superType = MetadataHelper.asSuper(s, t);
                if (superType != null && StringUtilities.equals(superType.getInternalName(), s.getInternalName()) && (!(s instanceof IGenericInstance) || MetadataHelper.access$0(s, superType)) && MetadataHelper.isSubTypeNoCapture(superType.getDeclaringType(), s.getDeclaringType())) {
                    return true;
                }
                return false;
            }
            
            @Override
            public Boolean visitCompoundType(final CompoundTypeReference t, final TypeReference s) {
                return super.visitCompoundType(t, s);
            }
            
            @Override
            public Boolean visitGenericParameter(final GenericParameter t, final TypeReference s) {
                return MetadataHelper.isSubTypeNoCapture(t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object, s);
            }
            
            @Override
            public Boolean visitParameterizedType(final TypeReference t, final TypeReference s) {
                return this.visitClassType(t, s);
            }
            
            @Override
            public Boolean visitPrimitiveType(final PrimitiveType t, final TypeReference s) {
                final JvmType jt = t.getSimpleType();
                final JvmType js = s.getSimpleType();
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[js.ordinal()]) {
                    case 1: {
                        if (jt == JvmType.Boolean) {
                            return true;
                        }
                        return false;
                    }
                    case 2: {
                        if (js != JvmType.Character && jt.isIntegral() && jt.bitWidth() <= js.bitWidth()) {
                            return true;
                        }
                        return false;
                    }
                    case 3: {
                        if (jt == JvmType.Character) {
                            return true;
                        }
                        return false;
                    }
                    case 4: {
                        if (jt == JvmType.Character) {
                            return false;
                        }
                    }
                    case 5:
                    case 6: {
                        if (jt.isIntegral() && jt.bitWidth() <= js.bitWidth()) {
                            return true;
                        }
                        return false;
                    }
                    case 7:
                    case 8: {
                        if (!jt.isIntegral() && jt.bitWidth() > js.bitWidth()) {
                            return false;
                        }
                        return true;
                    }
                    case 13: {
                        if (s.getSimpleType() == JvmType.Void) {
                            return true;
                        }
                        return false;
                    }
                    default: {
                        return Boolean.FALSE;
                    }
                }
            }
            
            @Override
            public Boolean visitRawType(final RawType t, final TypeReference s) {
                return this.visitClassType(t, s);
            }
            
            @Override
            public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
                return Boolean.FALSE;
            }
            
            @Override
            public Boolean visitCapturedType(final CapturedType t, final TypeReference s) {
                return MetadataHelper.isSubTypeNoCapture(t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object, s);
            }
            
            @Override
            public Boolean visitType(final TypeReference t, final TypeReference s) {
                return Boolean.FALSE;
            }
            
            static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
                final int[] loc_0 = MetadataHelper$5.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
                if (loc_0 != null) {
                    return loc_0;
                }
                final int[] loc_1 = new int[JvmType.values().length];
                try {
                    loc_1[JvmType.Array.ordinal()] = 10;
                }
                catch (NoSuchFieldError loc_2) {}
                try {
                    loc_1[JvmType.Boolean.ordinal()] = 1;
                }
                catch (NoSuchFieldError loc_3) {}
                try {
                    loc_1[JvmType.Byte.ordinal()] = 2;
                }
                catch (NoSuchFieldError loc_4) {}
                try {
                    loc_1[JvmType.Character.ordinal()] = 3;
                }
                catch (NoSuchFieldError loc_5) {}
                try {
                    loc_1[JvmType.Double.ordinal()] = 8;
                }
                catch (NoSuchFieldError loc_6) {}
                try {
                    loc_1[JvmType.Float.ordinal()] = 7;
                }
                catch (NoSuchFieldError loc_7) {}
                try {
                    loc_1[JvmType.Integer.ordinal()] = 5;
                }
                catch (NoSuchFieldError loc_8) {}
                try {
                    loc_1[JvmType.Long.ordinal()] = 6;
                }
                catch (NoSuchFieldError loc_9) {}
                try {
                    loc_1[JvmType.Object.ordinal()] = 9;
                }
                catch (NoSuchFieldError loc_10) {}
                try {
                    loc_1[JvmType.Short.ordinal()] = 4;
                }
                catch (NoSuchFieldError loc_11) {}
                try {
                    loc_1[JvmType.TypeVariable.ordinal()] = 11;
                }
                catch (NoSuchFieldError loc_12) {}
                try {
                    loc_1[JvmType.Void.ordinal()] = 13;
                }
                catch (NoSuchFieldError loc_13) {}
                try {
                    loc_1[JvmType.Wildcard.ordinal()] = 12;
                }
                catch (NoSuchFieldError loc_14) {}
                return MetadataHelper$5.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
            }
        };
        CONTAINS_TYPE_VISITOR = new TypeRelation() {
            private TypeReference U(final TypeReference t) {
                TypeReference current;
                for (current = t; current.isWildcardType(); current = current.getExtendsBound()) {
                    if (current.isUnbounded()) {
                        return BuiltinTypes.Object;
                    }
                    if (current.hasSuperBound()) {
                        return current.getSuperBound();
                    }
                }
                return current;
            }
            
            private TypeReference L(final TypeReference t) {
                TypeReference current;
                for (current = t; current.isWildcardType(); current = current.getSuperBound()) {
                    if (current.isUnbounded() || current.hasExtendsBound()) {
                        return BuiltinTypes.Bottom;
                    }
                }
                return current;
            }
            
            @Override
            public Boolean visitType(final TypeReference t, final TypeReference s) {
                return MetadataHelper.isSameType(t, s);
            }
            
            @Override
            public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
                if (!MetadataHelper.access$1(t, s) && !MetadataHelper.access$2(s, t) && ((!t.hasExtendsBound() && !MetadataHelper.isSubTypeNoCapture(this.L(t), MetadataHelper.getLowerBound(s))) || (!t.hasSuperBound() && !MetadataHelper.isSubTypeNoCapture(MetadataHelper.getUpperBound(s), this.U(t))))) {
                    return false;
                }
                return true;
            }
        };
        AS_SUPER_VISITOR = new TypeMapper<TypeReference>() {
            @Override
            public TypeReference visitType(final TypeReference t, final TypeReference s) {
                return null;
            }
            
            public TypeReference visitArrayType(final ArrayType t, final TypeReference s) {
                return MetadataHelper.isSubType(t, s) ? s : null;
            }
            
            public TypeReference visitClassType(final TypeReference t, final TypeReference s) {
                if (StringUtilities.equals(t.getInternalName(), s.getInternalName())) {
                    return t;
                }
                final TypeReference st = MetadataHelper.getSuperType(t);
                if (st != null && (st.getSimpleType() == JvmType.Object || st.getSimpleType() == JvmType.TypeVariable)) {
                    final TypeReference x = MetadataHelper.asSuper(s, st);
                    if (x != null) {
                        return x;
                    }
                }
                final TypeDefinition ds = s.resolve();
                if (ds != null && ds.isInterface()) {
                    for (final TypeReference i : MetadataHelper.getInterfaces(t)) {
                        final TypeReference x2 = MetadataHelper.asSuper(s, i);
                        if (x2 != null) {
                            return x2;
                        }
                    }
                }
                return null;
            }
            
            public TypeReference visitGenericParameter(final GenericParameter t, final TypeReference s) {
                if (MetadataHelper.isSameType(t, s)) {
                    return t;
                }
                return MetadataHelper.asSuper(s, t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object);
            }
            
            public TypeReference visitNullType(final TypeReference t, final TypeReference s) {
                return super.visitNullType(t, (T)s);
            }
            
            public TypeReference visitParameterizedType(final TypeReference t, final TypeReference s) {
                return this.visitClassType(t, s);
            }
            
            public TypeReference visitPrimitiveType(final PrimitiveType t, final TypeReference s) {
                return super.visitPrimitiveType(t, (T)s);
            }
            
            public TypeReference visitRawType(final RawType t, final TypeReference s) {
                return this.visitClassType(t, s);
            }
            
            public TypeReference visitWildcard(final WildcardType t, final TypeReference s) {
                return super.visitWildcard(t, (T)s);
            }
        };
        SUPER_VISITOR = new TypeMapper<Void>() {
            @Override
            public TypeReference visitType(final TypeReference t, final Void ignored) {
                return null;
            }
            
            public TypeReference visitArrayType(final ArrayType t, final Void ignored) {
                final TypeReference et = MetadataHelper.getElementType(t);
                if (et.isPrimitive() || MetadataHelper.isSameType(et, BuiltinTypes.Object)) {
                    return MetadataHelper.access$3(et);
                }
                final TypeReference superType = MetadataHelper.getSuperType(et);
                return (superType != null) ? superType.makeArrayType() : null;
            }
            
            public TypeReference visitCompoundType(final CompoundTypeReference t, final Void ignored) {
                final TypeReference bt = t.getBaseType();
                if (bt != null) {
                    return MetadataHelper.getSuperType(bt);
                }
                return t;
            }
            
            public TypeReference visitClassType(final TypeReference t, final Void ignored) {
                final TypeDefinition resolved = t.resolve();
                if (resolved == null) {
                    return BuiltinTypes.Object;
                }
                TypeReference superType;
                if (resolved.isInterface()) {
                    superType = resolved.getBaseType();
                    if (superType == null) {
                        superType = CollectionUtilities.firstOrDefault(resolved.getExplicitInterfaces());
                    }
                }
                else {
                    superType = resolved.getBaseType();
                }
                if (superType == null) {
                    return null;
                }
                if (!resolved.isGenericDefinition()) {
                    return superType;
                }
                if (!t.isGenericType()) {
                    return MetadataHelper.eraseRecursive(superType);
                }
                if (t.isGenericDefinition()) {
                    return superType;
                }
                return MetadataHelper.substituteGenericArguments(superType, MetadataHelper.access$4(t));
            }
            
            public TypeReference visitGenericParameter(final GenericParameter t, final Void ignored) {
                return t.hasExtendsBound() ? t.getExtendsBound() : BuiltinTypes.Object;
            }
            
            public TypeReference visitNullType(final TypeReference t, final Void ignored) {
                return BuiltinTypes.Object;
            }
            
            public TypeReference visitParameterizedType(final TypeReference t, final Void ignored) {
                return this.visitClassType(t, ignored);
            }
            
            public TypeReference visitRawType(final RawType t, final Void ignored) {
                TypeReference genericDefinition = t.getUnderlyingType();
                if (!genericDefinition.isGenericDefinition()) {
                    final TypeDefinition resolved = genericDefinition.resolve();
                    if (resolved == null || !resolved.isGenericDefinition()) {
                        return BuiltinTypes.Object;
                    }
                    genericDefinition = resolved;
                }
                final TypeReference baseType = MetadataHelper.getBaseType(genericDefinition);
                return (baseType != null && baseType.isGenericType()) ? MetadataHelper.eraseRecursive(baseType) : baseType;
            }
            
            public TypeReference visitWildcard(final WildcardType t, final Void ignored) {
                if (t.isUnbounded()) {
                    return BuiltinTypes.Object;
                }
                if (t.hasExtendsBound()) {
                    return t.getExtendsBound();
                }
                return null;
            }
        };
        SAME_TYPE_VISITOR_LOOSE = new LooseSameTypeVisitor();
        SAME_TYPE_VISITOR_STRICT = new StrictSameTypeVisitor();
        INTERFACES_VISITOR = new DefaultTypeVisitor<Void, List<TypeReference>>() {
            @Override
            public List<TypeReference> visitClassType(final TypeReference t, final Void ignored) {
                final TypeDefinition r = t.resolve();
                if (r == null) {
                    return Collections.emptyList();
                }
                final List<TypeReference> interfaces = r.getExplicitInterfaces();
                if (!r.isGenericDefinition()) {
                    return interfaces;
                }
                if (t.isGenericDefinition()) {
                    return interfaces;
                }
                if (MetadataHelper.isRawType(t)) {
                    return MetadataHelper.eraseRecursive(interfaces);
                }
                final List<? extends TypeReference> formal = MetadataHelper.access$5(r);
                final List<? extends TypeReference> actual = MetadataHelper.access$5(t);
                final ArrayList<TypeReference> result = new ArrayList<TypeReference>();
                final Map<TypeReference, TypeReference> mappings = new HashMap<TypeReference, TypeReference>();
                for (int i = 0, n = formal.size(); i < n; ++i) {
                    mappings.put((TypeReference)formal.get(i), (TypeReference)actual.get(i));
                }
                for (int i = 0, n = interfaces.size(); i < n; ++i) {
                    result.add(MetadataHelper.substituteGenericArguments(interfaces.get(i), mappings));
                }
                return result;
            }
            
            @Override
            public List<TypeReference> visitWildcard(final WildcardType t, final Void ignored) {
                if (t.hasExtendsBound()) {
                    final TypeReference bound = t.getExtendsBound();
                    final TypeDefinition resolvedBound = bound.resolve();
                    if (resolvedBound != null) {
                        if (resolvedBound.isInterface()) {
                            return Collections.singletonList(bound);
                        }
                        if (resolvedBound.isCompoundType()) {
                            ((DefaultTypeVisitor<Void, Object>)this).visit(bound, null);
                        }
                    }
                    return this.visit(bound, null);
                }
                return Collections.emptyList();
            }
            
            @Override
            public List<TypeReference> visitGenericParameter(final GenericParameter t, final Void ignored) {
                if (t.hasExtendsBound()) {
                    final TypeReference bound = t.getExtendsBound();
                    final TypeDefinition resolvedBound = bound.resolve();
                    if (resolvedBound != null) {
                        if (resolvedBound.isInterface()) {
                            return Collections.singletonList(bound);
                        }
                        if (resolvedBound.isCompoundType()) {
                            ((DefaultTypeVisitor<Void, Object>)this).visit(bound, null);
                        }
                    }
                    return this.visit(bound, null);
                }
                return Collections.emptyList();
            }
        };
        AS_SUBTYPE_VISITOR = new TypeMapper<TypeReference>() {
            public TypeReference visitClassType(final TypeReference t, final TypeReference s) {
                if (MetadataHelper.isSameType(t, s)) {
                    return t;
                }
                final TypeReference base = MetadataHelper.asSuper(t, s);
                if (base == null) {
                    return null;
                }
                Map<TypeReference, TypeReference> mappings;
                try {
                    mappings = MetadataHelper.adapt(base, t);
                }
                catch (AdaptFailure ignored) {
                    mappings = MetadataHelper.getGenericSubTypeMappings(t, base);
                }
                final TypeReference result = MetadataHelper.substituteGenericArguments(s, mappings);
                if (!MetadataHelper.isSubType(result, t)) {
                    return null;
                }
                final List<? extends TypeReference> tTypeArguments = MetadataHelper.access$5(t);
                final List<? extends TypeReference> sTypeArguments = MetadataHelper.access$5(s);
                final List<? extends TypeReference> resultTypeArguments = MetadataHelper.access$5(result);
                List<TypeReference> openGenericParameters = null;
                for (final TypeReference a : sTypeArguments) {
                    if (a.isGenericParameter() && CollectionUtilities.indexOfByIdentity(resultTypeArguments, a) >= 0 && CollectionUtilities.indexOfByIdentity(tTypeArguments, a) < 0) {
                        if (openGenericParameters == null) {
                            openGenericParameters = new ArrayList<TypeReference>();
                        }
                        openGenericParameters.add(a);
                    }
                }
                if (openGenericParameters == null) {
                    return result;
                }
                if (MetadataHelper.isRawType(t)) {
                    return MetadataHelper.eraseRecursive(result);
                }
                final Map<TypeReference, TypeReference> unboundMappings = new HashMap<TypeReference, TypeReference>();
                for (final TypeReference p : openGenericParameters) {
                    unboundMappings.put(p, WildcardType.unbounded());
                }
                return MetadataHelper.substituteGenericArguments(result, unboundMappings);
            }
        };
        ERASE_VISITOR = new DefaultTypeVisitor<Boolean, TypeReference>() {
            @Override
            public TypeReference visitArrayType(final ArrayType t, final Boolean recurse) {
                final TypeReference elementType = MetadataHelper.getElementType(t);
                final TypeReference erasedElementType = MetadataHelper.erase(MetadataHelper.getElementType(t), recurse);
                return (erasedElementType == elementType) ? t : erasedElementType.makeArrayType();
            }
            
            @Override
            public TypeReference visitBottomType(final TypeReference t, final Boolean recurse) {
                return t;
            }
            
            @Override
            public TypeReference visitClassType(final TypeReference t, final Boolean recurse) {
                if (t.isGenericType()) {
                    return new RawType(t);
                }
                final TypeDefinition resolved = t.resolve();
                if (resolved != null && resolved.isGenericDefinition()) {
                    return new RawType(resolved);
                }
                return t;
            }
            
            @Override
            public TypeReference visitCompoundType(final CompoundTypeReference t, final Boolean recurse) {
                final TypeReference baseType = t.getBaseType();
                return MetadataHelper.erase((baseType != null) ? baseType : CollectionUtilities.first(t.getInterfaces()), recurse);
            }
            
            @Override
            public TypeReference visitGenericParameter(final GenericParameter t, final Boolean recurse) {
                return MetadataHelper.erase(MetadataHelper.getUpperBound(t), recurse);
            }
            
            @Override
            public TypeReference visitNullType(final TypeReference t, final Boolean recurse) {
                return t;
            }
            
            @Override
            public TypeReference visitPrimitiveType(final PrimitiveType t, final Boolean recurse) {
                return t;
            }
            
            @Override
            public TypeReference visitRawType(final RawType t, final Boolean recurse) {
                return t;
            }
            
            @Override
            public TypeReference visitType(final TypeReference t, final Boolean recurse) {
                if (t.isGenericType()) {
                    return new RawType(t);
                }
                return t;
            }
            
            @Override
            public TypeReference visitWildcard(final WildcardType t, final Boolean recurse) {
                return MetadataHelper.erase(MetadataHelper.getUpperBound(t), recurse);
            }
        };
        IS_DECLARED_TYPE = new DefaultTypeVisitor<Void, Boolean>() {
            @Override
            public Boolean visitWildcard(final WildcardType t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitArrayType(final ArrayType t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitBottomType(final TypeReference t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitCapturedType(final CapturedType t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitClassType(final TypeReference t, final Void ignored) {
                return true;
            }
            
            @Override
            public Boolean visitCompoundType(final CompoundTypeReference t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitGenericParameter(final GenericParameter t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitNullType(final TypeReference t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitParameterizedType(final TypeReference t, final Void ignored) {
                return true;
            }
            
            @Override
            public Boolean visitPrimitiveType(final PrimitiveType t, final Void ignored) {
                return false;
            }
            
            @Override
            public Boolean visitRawType(final RawType t, final Void ignored) {
                return true;
            }
            
            @Override
            public Boolean visitType(final TypeReference t, final Void ignored) {
                return false;
            }
        };
    }
    
    public static boolean areGenericsSupported(final TypeDefinition t) {
        return t != null && t.getCompilerMajorVersion() >= 49;
    }
    
    public static int getArrayRank(final TypeReference t) {
        if (t == null) {
            return 0;
        }
        int rank = 0;
        for (TypeReference current = t; current.isArray(); current = current.getElementType()) {
            ++rank;
        }
        return rank;
    }
    
    public static boolean isEnclosedBy(final TypeReference innerType, final TypeReference outerType) {
        if (innerType == null) {
            return false;
        }
        for (TypeReference current = innerType; current != null; current = current.getDeclaringType()) {
            if (isSameType(current, outerType)) {
                return true;
            }
        }
        final TypeDefinition resolvedInnerType = innerType.resolve();
        return resolvedInnerType != null && isEnclosedBy(resolvedInnerType.getBaseType(), outerType);
    }
    
    public static boolean canReferenceTypeVariablesOf(final TypeReference declaringType, final TypeReference referenceSite) {
        if (declaringType == null || referenceSite == null) {
            return false;
        }
        if (declaringType == referenceSite) {
            return declaringType.isGenericType();
        }
        TypeReference current = referenceSite.getDeclaringType();
        while (current != null) {
            if (isSameType(current, declaringType)) {
                return true;
            }
            final TypeDefinition resolvedType = current.resolve();
            if (resolvedType != null) {
                final MethodReference declaringMethod = resolvedType.getDeclaringMethod();
                if (declaringMethod != null) {
                    current = declaringMethod.getDeclaringType();
                    continue;
                }
            }
            current = current.getDeclaringType();
        }
        return false;
    }
    
    public static TypeReference findCommonSuperType(final TypeReference type1, final TypeReference type2) {
        VerifyArgument.notNull(type1, "type1");
        VerifyArgument.notNull(type2, "type2");
        if (type1 == type2) {
            return type1;
        }
        if (type1.isPrimitive()) {
            if (!type2.isPrimitive()) {
                return findCommonSuperType(getBoxedTypeOrSelf(type1), type2);
            }
            if (isAssignableFrom(type1, type2)) {
                return type1;
            }
            if (isAssignableFrom(type2, type1)) {
                return type2;
            }
            return doNumericPromotion(type1, type2);
        }
        else {
            if (type2.isPrimitive()) {
                return findCommonSuperType(type1, getBoxedTypeOrSelf(type2));
            }
            int rank1 = 0;
            int rank2 = 0;
            TypeReference elementType1 = type1;
            TypeReference elementType2 = type2;
            while (elementType1.isArray()) {
                elementType1 = elementType1.getElementType();
                ++rank1;
            }
            while (elementType2.isArray()) {
                elementType2 = elementType2.getElementType();
                ++rank2;
            }
            if (rank1 != rank2) {
                return BuiltinTypes.Object;
            }
            if (rank1 == 0 || (!elementType1.isPrimitive() && !elementType2.isPrimitive())) {
                while (!elementType1.isUnbounded()) {
                    elementType1 = (elementType1.hasSuperBound() ? elementType1.getSuperBound() : elementType1.getExtendsBound());
                }
                while (!elementType2.isUnbounded()) {
                    elementType2 = (elementType2.hasSuperBound() ? elementType2.getSuperBound() : elementType2.getExtendsBound());
                }
                TypeReference result = findCommonSuperTypeCore(elementType1, elementType2);
                while (rank1-- > 0) {
                    result = result.makeArrayType();
                }
                return result;
            }
            if (elementType1.isPrimitive() && elementType2.isPrimitive()) {
                TypeReference promotedType = doNumericPromotion(elementType1, elementType2);
                while (rank1-- > 0) {
                    promotedType = promotedType.makeArrayType();
                }
                return promotedType;
            }
            return BuiltinTypes.Object;
        }
    }
    
    private static TypeReference doNumericPromotion(final TypeReference leftType, final TypeReference rightType) {
        final JvmType left = leftType.getSimpleType();
        final JvmType right = rightType.getSimpleType();
        if (left == right) {
            return leftType;
        }
        if (left == JvmType.Double || right == JvmType.Double) {
            return BuiltinTypes.Double;
        }
        if (left == JvmType.Float || right == JvmType.Float) {
            return BuiltinTypes.Float;
        }
        if (left == JvmType.Long || right == JvmType.Long) {
            return BuiltinTypes.Long;
        }
        if ((left.isNumeric() && left != JvmType.Boolean) || (right.isNumeric() && right != JvmType.Boolean)) {
            return BuiltinTypes.Integer;
        }
        return leftType;
    }
    
    private static TypeReference findCommonSuperTypeCore(final TypeReference type1, final TypeReference type2) {
        if (isAssignableFrom(type1, type2)) {
            if (type2.isGenericType() && !type1.isGenericType()) {
                final TypeDefinition resolved1 = type1.resolve();
                if (resolved1 != null) {
                    return substituteGenericArguments(resolved1, type2);
                }
            }
            return substituteGenericArguments(type1, type2);
        }
        if (isAssignableFrom(type2, type1)) {
            if (type1.isGenericType() && !type2.isGenericType()) {
                final TypeDefinition resolved2 = type2.resolve();
                if (resolved2 != null) {
                    return substituteGenericArguments(resolved2, type1);
                }
            }
            return substituteGenericArguments(type2, type1);
        }
        final TypeDefinition c = type1.resolve();
        final TypeDefinition d = type2.resolve();
        if (c == null || d == null || c.isInterface() || d.isInterface()) {
            return BuiltinTypes.Object;
        }
        TypeReference current = c;
        while (current != null) {
            for (final TypeReference interfaceType : getInterfaces(current)) {
                if (isAssignableFrom(interfaceType, d)) {
                    return interfaceType;
                }
            }
            current = getBaseType(current);
            if (current != null && isAssignableFrom(current, d)) {
                return current;
            }
        }
        return BuiltinTypes.Object;
    }
    
    public static ConversionType getConversionType(final TypeReference target, final TypeReference source) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");
        final TypeReference underlyingTarget = getUnderlyingPrimitiveTypeOrSelf(target);
        final TypeReference underlyingSource = getUnderlyingPrimitiveTypeOrSelf(source);
        if (underlyingTarget.getSimpleType().isNumeric() && underlyingSource.getSimpleType().isNumeric()) {
            return getNumericConversionType(target, source);
        }
        if (StringUtilities.equals(target.getInternalName(), "java/lang/Object")) {
            return ConversionType.IMPLICIT;
        }
        if (isSameType(target, source, true)) {
            return ConversionType.IDENTITY;
        }
        if (isAssignableFrom(target, source, false)) {
            return ConversionType.IMPLICIT;
        }
        int targetRank = 0;
        int sourceRank = 0;
        TypeReference targetElementType = target;
        TypeReference sourceElementType = source;
        while (targetElementType.isArray()) {
            ++targetRank;
            targetElementType = targetElementType.getElementType();
        }
        while (sourceElementType.isArray()) {
            ++sourceRank;
            sourceElementType = sourceElementType.getElementType();
        }
        if (sourceRank == targetRank) {
            return ConversionType.EXPLICIT;
        }
        if (isSameType(sourceElementType, BuiltinTypes.Object)) {
            return ConversionType.EXPLICIT;
        }
        return ConversionType.NONE;
    }
    
    public static ConversionType getNumericConversionType(final TypeReference target, final TypeReference source) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");
        if (isSameType(target, source)) {
            return ConversionType.IDENTITY;
        }
        if (!source.isPrimitive()) {
            TypeReference unboxedSourceType = null;
            Label_0284: {
                final String loc_0;
                switch (loc_0 = source.getInternalName()) {
                    case "java/lang/Integer": {
                        unboxedSourceType = BuiltinTypes.Integer;
                        break Label_0284;
                    }
                    case "java/lang/Byte": {
                        unboxedSourceType = BuiltinTypes.Byte;
                        break Label_0284;
                    }
                    case "java/lang/Long": {
                        unboxedSourceType = BuiltinTypes.Long;
                        break Label_0284;
                    }
                    case "java/lang/Character": {
                        unboxedSourceType = BuiltinTypes.Character;
                        break Label_0284;
                    }
                    case "java/lang/Double": {
                        unboxedSourceType = BuiltinTypes.Double;
                        break Label_0284;
                    }
                    case "java/lang/Boolean": {
                        unboxedSourceType = BuiltinTypes.Boolean;
                        break Label_0284;
                    }
                    case "java/lang/Float": {
                        unboxedSourceType = BuiltinTypes.Float;
                        break Label_0284;
                    }
                    case "java/lang/Short": {
                        unboxedSourceType = BuiltinTypes.Short;
                        break Label_0284;
                    }
                    default:
                        break;
                }
                return ConversionType.NONE;
            }
            final ConversionType unboxedConversion = getNumericConversionType(target, unboxedSourceType);
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType()[unboxedConversion.ordinal()]) {
                case 1:
                case 2: {
                    return ConversionType.IMPLICIT;
                }
                case 3: {
                    return ConversionType.NONE;
                }
                default: {
                    return unboxedConversion;
                }
            }
        }
        else if (!target.isPrimitive()) {
            TypeReference unboxedTargetType = null;
            Label_0592: {
                final String loc_1;
                switch (loc_1 = target.getInternalName()) {
                    case "java/lang/Integer": {
                        unboxedTargetType = BuiltinTypes.Integer;
                        break Label_0592;
                    }
                    case "java/lang/Byte": {
                        unboxedTargetType = BuiltinTypes.Byte;
                        break Label_0592;
                    }
                    case "java/lang/Long": {
                        unboxedTargetType = BuiltinTypes.Long;
                        break Label_0592;
                    }
                    case "java/lang/Character": {
                        unboxedTargetType = BuiltinTypes.Character;
                        break Label_0592;
                    }
                    case "java/lang/Double": {
                        unboxedTargetType = BuiltinTypes.Double;
                        break Label_0592;
                    }
                    case "java/lang/Boolean": {
                        unboxedTargetType = BuiltinTypes.Boolean;
                        break Label_0592;
                    }
                    case "java/lang/Float": {
                        unboxedTargetType = BuiltinTypes.Float;
                        break Label_0592;
                    }
                    case "java/lang/Short": {
                        unboxedTargetType = BuiltinTypes.Short;
                        break Label_0592;
                    }
                    default:
                        break;
                }
                return ConversionType.NONE;
            }
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType()[getNumericConversionType(unboxedTargetType, source).ordinal()]) {
                case 1: {
                    return ConversionType.IMPLICIT;
                }
                case 2: {
                    return ConversionType.EXPLICIT_TO_UNBOXED;
                }
                case 3: {
                    return ConversionType.EXPLICIT;
                }
                default: {
                    return ConversionType.NONE;
                }
            }
        }
        else {
            final JvmType targetJvmType = target.getSimpleType();
            final JvmType sourceJvmType = source.getSimpleType();
            if (targetJvmType == sourceJvmType) {
                return ConversionType.IDENTITY;
            }
            if (sourceJvmType == JvmType.Boolean) {
                return ConversionType.NONE;
            }
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[targetJvmType.ordinal()]) {
                case 7:
                case 8: {
                    if (sourceJvmType.isIntegral() || sourceJvmType.bitWidth() <= targetJvmType.bitWidth()) {
                        return ConversionType.IMPLICIT;
                    }
                    return ConversionType.EXPLICIT;
                }
                case 2:
                case 4: {
                    if (sourceJvmType == JvmType.Character) {
                        return ConversionType.EXPLICIT;
                    }
                }
                case 5:
                case 6: {
                    if (sourceJvmType.isIntegral() && sourceJvmType.bitWidth() <= targetJvmType.bitWidth()) {
                        return ConversionType.IMPLICIT;
                    }
                    return ConversionType.EXPLICIT;
                }
                case 3: {
                    return sourceJvmType.isNumeric() ? ConversionType.EXPLICIT : ConversionType.NONE;
                }
                default: {
                    return ConversionType.NONE;
                }
            }
        }
    }
    
    public static boolean hasImplicitNumericConversion(final TypeReference target, final TypeReference source) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");
        if (target == source) {
            return true;
        }
        if (!target.isPrimitive() || !source.isPrimitive()) {
            return false;
        }
        final JvmType targetJvmType = target.getSimpleType();
        final JvmType sourceJvmType = source.getSimpleType();
        if (targetJvmType == sourceJvmType) {
            return true;
        }
        if (sourceJvmType == JvmType.Boolean) {
            return false;
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[targetJvmType.ordinal()]) {
            case 7:
            case 8: {
                return sourceJvmType.bitWidth() <= targetJvmType.bitWidth();
            }
            case 2:
            case 4:
            case 5:
            case 6: {
                return sourceJvmType.isIntegral() && sourceJvmType.bitWidth() <= targetJvmType.bitWidth();
            }
            default: {
                return false;
            }
        }
    }
    
    public static boolean isConvertible(final TypeReference source, final TypeReference target) {
        return isConvertible(source, target, true);
    }
    
    public static boolean isConvertible(final TypeReference source, final TypeReference target, final boolean allowUnchecked) {
        VerifyArgument.notNull(source, "source");
        VerifyArgument.notNull(target, "target");
        final boolean tPrimitive = target.isPrimitive();
        final boolean sPrimitive = source.isPrimitive();
        if (source == BuiltinTypes.Null) {
            return !tPrimitive;
        }
        if (target.isWildcardType() && target.isUnbounded()) {
            return !sPrimitive;
        }
        if (tPrimitive == sPrimitive) {
            return allowUnchecked ? isSubTypeUnchecked(source, target) : isSubType(source, target);
        }
        if (!tPrimitive) {
            return allowUnchecked ? isSubTypeUnchecked(getBoxedTypeOrSelf(source), target) : isSubType(getBoxedTypeOrSelf(source), target);
        }
        switch ($SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType()[getNumericConversionType(target, source).ordinal()]) {
            case 1:
            case 2: {
                return true;
            }
            default: {
                return false;
            }
        }
    }
    
    private static boolean isSubTypeUnchecked(final TypeReference t, final TypeReference s) {
        return isSubtypeUncheckedInternal(t, s);
    }
    
    private static boolean isSubtypeUncheckedInternal(final TypeReference t, final TypeReference s) {
        if (t == s) {
            return true;
        }
        if (t == null || s == null) {
            return false;
        }
        if (t.isArray() && s.isArray()) {
            if (t.getElementType().isPrimitive()) {
                return isSameType(getElementType(t), getElementType(s));
            }
            return isSubTypeUnchecked(getElementType(t), getElementType(s));
        }
        else {
            if (isSubType(t, s)) {
                return true;
            }
            if (t.isGenericParameter() && t.hasExtendsBound()) {
                return isSubTypeUnchecked(getUpperBound(t), s);
            }
            if (!isRawType(s)) {
                final TypeReference t2 = asSuper(s, t);
                if (t2 != null && isRawType(t2)) {
                    return true;
                }
            }
            return false;
        }
    }
    
    public static boolean isAssignableFrom(final TypeReference target, final TypeReference source) {
        return isConvertible(source, target);
    }
    
    public static boolean isAssignableFrom(final TypeReference target, final TypeReference source, final boolean allowUnchecked) {
        return isConvertible(source, target, allowUnchecked);
    }
    
    public static boolean isSubType(final TypeReference type, final TypeReference baseType) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(baseType, "baseType");
        return isSubType(type, baseType, true);
    }
    
    public static boolean isPrimitiveBoxType(final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        final String loc_0;
        switch ((loc_0 = type.getInternalName()).hashCode()) {
            case -607409974: {
                if (!loc_0.equals("java/lang/Integer")) {
                    return false;
                }
                break;
            }
            case 202917116: {
                if (!loc_0.equals("java/lang/Byte")) {
                    return false;
                }
                break;
            }
            case 203205232: {
                if (!loc_0.equals("java/lang/Long")) {
                    return false;
                }
                break;
            }
            case 203502984: {
                if (!loc_0.equals("java/lang/Void")) {
                    return false;
                }
                break;
            }
            case 1466314677: {
                if (!loc_0.equals("java/lang/Character")) {
                    return false;
                }
                break;
            }
            case 1777873605: {
                if (!loc_0.equals("java/lang/Double")) {
                    return false;
                }
                break;
            }
            case 1794216884: {
                if (!loc_0.equals("java/lang/Boolean")) {
                    return false;
                }
                break;
            }
            case 1998765288: {
                if (!loc_0.equals("java/lang/Float")) {
                    return false;
                }
                break;
            }
            case 2010652424: {
                if (!loc_0.equals("java/lang/Short")) {
                    return false;
                }
                break;
            }
        }
        return true;
    }
    
    public static TypeReference getBoxedTypeOrSelf(final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        if (type.isPrimitive()) {
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[type.getSimpleType().ordinal()]) {
                case 1: {
                    return CommonTypeReferences.Boolean;
                }
                case 2: {
                    return CommonTypeReferences.Byte;
                }
                case 3: {
                    return CommonTypeReferences.Character;
                }
                case 4: {
                    return CommonTypeReferences.Short;
                }
                case 5: {
                    return CommonTypeReferences.Integer;
                }
                case 6: {
                    return CommonTypeReferences.Long;
                }
                case 7: {
                    return CommonTypeReferences.Float;
                }
                case 8: {
                    return CommonTypeReferences.Double;
                }
                case 13: {
                    return CommonTypeReferences.Void;
                }
            }
        }
        return type;
    }
    
    public static TypeReference getUnderlyingPrimitiveTypeOrSelf(final TypeReference type) {
        VerifyArgument.notNull(type, "type");
        final String loc_0;
        switch (loc_0 = type.getInternalName()) {
            case "java/lang/Integer": {
                return BuiltinTypes.Integer;
            }
            case "java/lang/Byte": {
                return BuiltinTypes.Byte;
            }
            case "java/lang/Long": {
                return BuiltinTypes.Long;
            }
            case "java/lang/Void": {
                return BuiltinTypes.Void;
            }
            case "java/lang/Character": {
                return BuiltinTypes.Character;
            }
            case "java/lang/Double": {
                return BuiltinTypes.Double;
            }
            case "java/lang/Boolean": {
                return BuiltinTypes.Boolean;
            }
            case "java/lang/Float": {
                return BuiltinTypes.Float;
            }
            case "java/lang/Short": {
                return BuiltinTypes.Short;
            }
            default:
                break;
        }
        return type;
    }
    
    public static TypeReference getDeclaredType(final TypeReference type) {
        if (type == null) {
            return null;
        }
        final TypeDefinition resolvedType = type.resolve();
        if (resolvedType == null) {
            return type;
        }
        if (resolvedType.isAnonymous()) {
            final List<TypeReference> interfaces = resolvedType.getExplicitInterfaces();
            final TypeReference baseType = interfaces.isEmpty() ? resolvedType.getBaseType() : interfaces.get(0);
            if (baseType != null) {
                final TypeReference asSuperType = asSuper(baseType, type);
                if (asSuperType != null) {
                    return asSuperType;
                }
                return baseType.isGenericType() ? new RawType(baseType) : baseType;
            }
        }
        return type;
    }
    
    public static TypeReference getBaseType(final TypeReference type) {
        if (type == null) {
            return null;
        }
        final TypeDefinition resolvedType = type.resolve();
        if (resolvedType == null) {
            return null;
        }
        final TypeReference baseType = resolvedType.getBaseType();
        if (baseType == null) {
            return null;
        }
        return substituteGenericArguments(baseType, type);
    }
    
    public static List<TypeReference> getInterfaces(final TypeReference type) {
        final List<TypeReference> result = MetadataHelper.INTERFACES_VISITOR.visit(type);
        return (result != null) ? result : Collections.emptyList();
    }
    
    public static TypeReference asSubType(final TypeReference type, final TypeReference baseType) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(baseType, "baseType");
        TypeReference effectiveType = type;
        if (type instanceof RawType) {
            effectiveType = type.getUnderlyingType();
        }
        else if (isRawType(type)) {
            final TypeDefinition resolvedType = type.resolve();
            effectiveType = ((resolvedType != null) ? resolvedType : type);
        }
        return MetadataHelper.AS_SUBTYPE_VISITOR.visit(baseType, effectiveType);
    }
    
    public static TypeReference asSuper(final TypeReference type, final TypeReference subType) {
        VerifyArgument.notNull(subType, "t");
        VerifyArgument.notNull(type, "s");
        return MetadataHelper.AS_SUPER_VISITOR.visit(subType, type);
    }
    
    public static Map<TypeReference, TypeReference> getGenericSubTypeMappings(final TypeReference type, final TypeReference baseType) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(baseType, "baseType");
        if (type.isArray() && baseType.isArray()) {
            TypeReference elementType;
            TypeReference baseElementType;
            for (elementType = type.getElementType(), baseElementType = baseType.getElementType(); elementType.isArray() && baseElementType.isArray(); elementType = elementType.getElementType(), baseElementType = baseElementType.getElementType()) {}
            return getGenericSubTypeMappings(elementType, baseElementType);
        }
        TypeReference current = type;
        List<? extends TypeReference> baseArguments;
        if (baseType.isGenericDefinition()) {
            baseArguments = baseType.getGenericParameters();
        }
        else if (baseType.isGenericType()) {
            baseArguments = ((IGenericInstance)baseType).getTypeArguments();
        }
        else {
            baseArguments = Collections.emptyList();
        }
        final TypeDefinition resolvedBaseType = baseType.resolve();
        while (current != null) {
            final TypeDefinition resolved = current.resolve();
            if (resolvedBaseType != null && resolvedBaseType.isGenericDefinition() && isSameType(resolved, resolvedBaseType)) {
                if (current instanceof IGenericInstance && baseType instanceof IGenericInstance) {
                    final List<? extends TypeReference> typeArguments = ((IGenericInstance)current).getTypeArguments();
                    if (baseArguments.size() == typeArguments.size()) {
                        final Map<TypeReference, TypeReference> map = new HashMap<TypeReference, TypeReference>();
                        for (int i = 0; i < typeArguments.size(); ++i) {
                            map.put((TypeReference)typeArguments.get(i), (TypeReference)baseArguments.get(i));
                        }
                        return map;
                    }
                }
                else if (baseType instanceof IGenericInstance && resolved.isGenericDefinition()) {
                    final List<GenericParameter> genericParameters = resolved.getGenericParameters();
                    final List<? extends TypeReference> typeArguments2 = ((IGenericInstance)baseType).getTypeArguments();
                    if (genericParameters.size() == typeArguments2.size()) {
                        final Map<TypeReference, TypeReference> map2 = new HashMap<TypeReference, TypeReference>();
                        for (int j = 0; j < typeArguments2.size(); ++j) {
                            map2.put(genericParameters.get(j), (TypeReference)typeArguments2.get(j));
                        }
                        return map2;
                    }
                }
            }
            if (resolvedBaseType != null && resolvedBaseType.isInterface()) {
                for (final TypeReference interfaceType : getInterfaces(current)) {
                    final Map<TypeReference, TypeReference> interfaceMap = getGenericSubTypeMappings(interfaceType, baseType);
                    if (!interfaceMap.isEmpty()) {
                        return interfaceMap;
                    }
                }
            }
            current = getBaseType(current);
        }
        return Collections.emptyMap();
    }
    
    public static MethodReference asMemberOf(final MethodReference method, final TypeReference baseType) {
        VerifyArgument.notNull(method, "method");
        VerifyArgument.notNull(baseType, "baseType");
        TypeReference base = baseType;
        MethodReference asMember;
        if (baseType instanceof RawType) {
            asMember = erase(method);
        }
        else {
            while (base.isGenericParameter() || base.isWildcardType()) {
                if (base.hasExtendsBound()) {
                    base = getUpperBound(base);
                }
                else {
                    base = BuiltinTypes.Object;
                }
            }
            final TypeReference asSuper = asSuper(method.getDeclaringType(), base);
            Map<TypeReference, TypeReference> map;
            try {
                map = adapt(method.getDeclaringType(), (asSuper != null) ? asSuper : base);
            }
            catch (AdaptFailure ignored) {
                map = getGenericSubTypeMappings(method.getDeclaringType(), (asSuper != null) ? asSuper : base);
            }
            asMember = TypeSubstitutionVisitor.instance().visitMethod(method, map);
            if (asMember != method && asMember instanceof GenericMethodInstance) {
                ((GenericMethodInstance)asMember).setDeclaringType((asSuper != null) ? asSuper : base);
            }
        }
        final MethodReference result = specializeIfNecessary(method, asMember, base);
        return result;
    }
    
    private static MethodReference specializeIfNecessary(final MethodReference originalMethod, final MethodReference asMember, final TypeReference baseType) {
        if (baseType.isArray() && StringUtilities.equals(asMember.getName(), "clone") && asMember.getParameters().isEmpty()) {
            return ensureReturnType(originalMethod, asMember, baseType, baseType);
        }
        if (!StringUtilities.equals(asMember.getName(), "getClass") || !asMember.getParameters().isEmpty()) {
            return asMember;
        }
        TypeDefinition resolvedType = baseType.resolve();
        TypeReference classType;
        TypeDefinition resolvedClassType;
        if (resolvedType == null || (classType = resolvedType.getResolver().lookupType("java/lang/Class")) == null || (resolvedClassType = classType.resolve()) == null) {
            resolvedType = originalMethod.getDeclaringType().resolve();
        }
        if (resolvedType == null || (classType = resolvedType.getResolver().lookupType("java/lang/Class")) == null || (resolvedClassType = classType.resolve()) == null) {
            return asMember;
        }
        if (resolvedClassType.isGenericType()) {
            final MethodDefinition resolvedMethod = originalMethod.resolve();
            return new GenericMethodInstance(baseType, (resolvedMethod != null) ? resolvedMethod : asMember, resolvedClassType.makeGenericType(WildcardType.makeExtends(erase(baseType))), Collections.emptyList(), Collections.emptyList());
        }
        return asMember;
    }
    
    private static MethodReference ensureReturnType(final MethodReference originalMethod, final MethodReference method, final TypeReference returnType, final TypeReference declaringType) {
        if (isSameType(method.getReturnType(), returnType, true)) {
            return method;
        }
        final MethodDefinition resolvedMethod = originalMethod.resolve();
        List<TypeReference> typeArguments;
        if (method instanceof IGenericInstance && method.isGenericMethod()) {
            typeArguments = ((IGenericInstance)method).getTypeArguments();
        }
        else {
            typeArguments = Collections.emptyList();
        }
        return new GenericMethodInstance(declaringType, (resolvedMethod != null) ? resolvedMethod : originalMethod, returnType, copyParameters(method.getParameters()), typeArguments);
    }
    
    public static FieldReference asMemberOf(final FieldReference field, final TypeReference baseType) {
        VerifyArgument.notNull(field, "field");
        VerifyArgument.notNull(baseType, "baseType");
        final Map<TypeReference, TypeReference> map = adapt(field.getDeclaringType(), baseType);
        return TypeSubstitutionVisitor.instance().visitField(field, map);
    }
    
    public static TypeReference substituteGenericArguments(final TypeReference inputType, final TypeReference substitutionsProvider) {
        if (inputType == null || substitutionsProvider == null) {
            return inputType;
        }
        return substituteGenericArguments(inputType, adapt(inputType, substitutionsProvider));
    }
    
    public static TypeReference substituteGenericArguments(final TypeReference inputType, final MethodReference substitutionsProvider) {
        if (inputType == null) {
            return null;
        }
        if (substitutionsProvider == null || !isGenericSubstitutionNeeded(inputType)) {
            return inputType;
        }
        final TypeReference declaringType = substitutionsProvider.getDeclaringType();
        assert declaringType != null;
        if (!substitutionsProvider.isGenericMethod() && !declaringType.isGenericType()) {
            return null;
        }
        List<? extends TypeReference> methodGenericParameters;
        if (substitutionsProvider.isGenericMethod()) {
            methodGenericParameters = substitutionsProvider.getGenericParameters();
        }
        else {
            methodGenericParameters = Collections.emptyList();
        }
        List<? extends TypeReference> methodTypeArguments;
        if (substitutionsProvider.isGenericDefinition()) {
            methodTypeArguments = methodGenericParameters;
        }
        else {
            methodTypeArguments = ((IGenericInstance)substitutionsProvider).getTypeArguments();
        }
        List<? extends TypeReference> genericParameters;
        List<? extends TypeReference> typeArguments;
        if (declaringType.isGenericType()) {
            genericParameters = declaringType.getGenericParameters();
            if (declaringType.isGenericDefinition()) {
                typeArguments = genericParameters;
            }
            else {
                typeArguments = ((IGenericInstance)declaringType).getTypeArguments();
            }
        }
        else {
            genericParameters = Collections.emptyList();
            typeArguments = Collections.emptyList();
        }
        if (methodTypeArguments.isEmpty() && typeArguments.isEmpty()) {
            return inputType;
        }
        final Map<TypeReference, TypeReference> map = new HashMap<TypeReference, TypeReference>();
        if (methodTypeArguments.size() == methodGenericParameters.size()) {
            for (int i = 0; i < methodTypeArguments.size(); ++i) {
                map.put((TypeReference)methodGenericParameters.get(i), (TypeReference)methodTypeArguments.get(i));
            }
        }
        if (typeArguments.size() == genericParameters.size()) {
            for (int i = 0; i < typeArguments.size(); ++i) {
                map.put((TypeReference)genericParameters.get(i), (TypeReference)typeArguments.get(i));
            }
        }
        return substituteGenericArguments(inputType, map);
    }
    
    public static TypeReference substituteGenericArguments(final TypeReference inputType, final Map<TypeReference, TypeReference> substitutionsProvider) {
        if (inputType == null) {
            return null;
        }
        if (substitutionsProvider == null || substitutionsProvider.isEmpty()) {
            return inputType;
        }
        return TypeSubstitutionVisitor.instance().visit(inputType, substitutionsProvider);
    }
    
    private static boolean isGenericSubstitutionNeeded(final TypeReference type) {
        if (type == null) {
            return false;
        }
        final TypeDefinition resolvedType = type.resolve();
        return resolvedType != null && resolvedType.containsGenericParameters();
    }
    
    public static List<MethodReference> findMethods(final TypeReference type) {
        return findMethods(type, Predicates.alwaysTrue());
    }
    
    public static List<MethodReference> findMethods(final TypeReference type, final Predicate<? super MethodReference> filter) {
        return findMethods(type, filter, false);
    }
    
    public static List<MethodReference> findMethods(final TypeReference type, final Predicate<? super MethodReference> filter, final boolean includeBridgeMethods) {
        return findMethods(type, filter, includeBridgeMethods, false);
    }
    
    public static List<MethodReference> findMethods(final TypeReference type, final Predicate<? super MethodReference> filter, final boolean includeBridgeMethods, final boolean includeOverriddenMethods) {
        VerifyArgument.notNull(type, "type");
        VerifyArgument.notNull(filter, "filter");
        final Set<String> descriptors = new HashSet<String>();
        final ArrayDeque<TypeReference> agenda = new ArrayDeque<TypeReference>();
        List<MethodReference> results = null;
        agenda.addLast(getUpperBound(type));
        descriptors.add(type.getInternalName());
        while (!agenda.isEmpty()) {
            final TypeDefinition resolvedType = agenda.removeFirst().resolve();
            if (resolvedType == null) {
                break;
            }
            final TypeReference baseType = resolvedType.getBaseType();
            if (baseType != null && descriptors.add(baseType.getInternalName())) {
                agenda.addLast(baseType);
            }
            for (final TypeReference interfaceType : resolvedType.getExplicitInterfaces()) {
                if (interfaceType != null && descriptors.add(interfaceType.getInternalName())) {
                    agenda.addLast(interfaceType);
                }
            }
            for (final MethodDefinition method : resolvedType.getDeclaredMethods()) {
                if (!includeBridgeMethods && method.isBridgeMethod()) {
                    continue;
                }
                if (!filter.test(method)) {
                    continue;
                }
                final String key = String.valueOf(includeOverriddenMethods ? method.getFullName() : method.getName()) + ":" + method.getErasedSignature();
                if (!descriptors.add(key)) {
                    continue;
                }
                if (results == null) {
                    results = new ArrayList<MethodReference>();
                }
                final MethodReference asMember = asMemberOf(method, type);
                results.add((asMember != null) ? asMember : method);
            }
        }
        return (results != null) ? results : Collections.emptyList();
    }
    
    public static boolean isOverloadCheckingRequired(final MethodReference method) {
        final MethodDefinition resolved = method.resolve();
        final boolean isVarArgs = resolved != null && resolved.isVarArgs();
        final TypeReference declaringType = ((resolved != null) ? resolved : method).getDeclaringType();
        final int parameterCount = ((resolved != null) ? resolved.getParameters() : method.getParameters()).size();
        final List<MethodReference> methods = findMethods(declaringType, Predicates.and(MetadataFilters.matchName(method.getName()), new Predicate<MethodReference>() {
            @Override
            public boolean test(final MethodReference m) {
                final List<ParameterDefinition> p = m.getParameters();
                final MethodDefinition r = (MethodDefinition)((m instanceof MethodDefinition) ? m : m.resolve());
                if (r != null && r.isBridgeMethod()) {
                    return false;
                }
                if (isVarArgs) {
                    return (r != null && r.isVarArgs()) || p.size() >= parameterCount;
                }
                if (p.size() < parameterCount) {
                    return r != null && r.isVarArgs();
                }
                return p.size() == parameterCount;
            }
        }));
        return methods.size() > 1;
    }
    
    public static TypeReference getLowerBound(final TypeReference t) {
        return MetadataHelper.LOWER_BOUND_VISITOR.visit(t);
    }
    
    public static TypeReference getUpperBound(final TypeReference t) {
        return MetadataHelper.UPPER_BOUND_VISITOR.visit(t);
    }
    
    public static TypeReference getElementType(final TypeReference t) {
        if (t.isArray()) {
            return t.getElementType();
        }
        if (t.isWildcardType()) {
            return getElementType(getUpperBound(t));
        }
        return null;
    }
    
    public static TypeReference getSuperType(final TypeReference t) {
        if (t == null) {
            return null;
        }
        return MetadataHelper.SUPER_VISITOR.visit(t);
    }
    
    public static boolean isSubTypeNoCapture(final TypeReference type, final TypeReference baseType) {
        return isSubType(type, baseType, false);
    }
    
    public static boolean isSubType(final TypeReference type, final TypeReference baseType, final boolean capture) {
        if (type == baseType) {
            return true;
        }
        if (type == null || baseType == null) {
            return false;
        }
        if (baseType instanceof CompoundTypeReference) {
            final CompoundTypeReference c = (CompoundTypeReference)baseType;
            if (!isSubType(type, getSuperType(c), capture)) {
                return false;
            }
            for (final TypeReference interfaceType : c.getInterfaces()) {
                if (!isSubType(type, interfaceType, capture)) {
                    return false;
                }
            }
            return true;
        }
        else {
            final TypeReference lower = getLowerBound(baseType);
            if (lower != baseType) {
                return isSubType(capture ? capture(type) : type, lower, false);
            }
            return MetadataHelper.IS_SUBTYPE_VISITOR.visit(capture ? capture(type) : type, baseType);
        }
    }
    
    private static TypeReference capture(final TypeReference type) {
        return type;
    }
    
    public static Map<TypeReference, TypeReference> adapt(final TypeReference source, final TypeReference target) {
        final Adapter adapter = new Adapter(null);
        ((DefaultTypeVisitor<TypeReference, Object>)adapter).visit(source, target);
        return adapter.mapping;
    }
    
    private static Map<TypeReference, TypeReference> adaptSelf(final TypeReference t) {
        final TypeDefinition r = t.resolve();
        return (r != null) ? adapt(r, t) : Collections.emptyMap();
    }
    
    private static TypeReference rewriteSupers(final TypeReference t) {
        if (!(t instanceof IGenericInstance)) {
            return t;
        }
        final Map<TypeReference, TypeReference> map = adaptSelf(t);
        if (map.isEmpty()) {
            return t;
        }
        Map<TypeReference, TypeReference> rewrite = null;
        for (final TypeReference k : map.keySet()) {
            final TypeReference original = map.get(k);
            TypeReference s = rewriteSupers(original);
            if (s.hasSuperBound() && !s.hasExtendsBound()) {
                s = WildcardType.unbounded();
                if (rewrite == null) {
                    rewrite = new HashMap<TypeReference, TypeReference>(map);
                }
            }
            else if (s != original) {
                s = WildcardType.makeExtends(getUpperBound(s));
                if (rewrite == null) {
                    rewrite = new HashMap<TypeReference, TypeReference>(map);
                }
            }
            if (rewrite != null) {
                map.put(k, s);
            }
        }
        if (rewrite != null) {
            return substituteGenericArguments(t, rewrite);
        }
        return t;
    }
    
    public static boolean containsType(final TypeReference t, final TypeReference s) {
        return MetadataHelper.CONTAINS_TYPE_VISITOR.visit(t, s);
    }
    
    public static boolean isSameType(final TypeReference t, final TypeReference s) {
        return isSameType(t, s, false);
    }
    
    public static boolean isSameType(final TypeReference t, final TypeReference s, final boolean strict) {
        return t == s || (t != null && s != null && (strict ? MetadataHelper.SAME_TYPE_VISITOR_STRICT.visit(t, s) : MetadataHelper.SAME_TYPE_VISITOR_LOOSE.visit(t, s)));
    }
    
    public static boolean areSameTypes(final List<? extends TypeReference> t, final List<? extends TypeReference> s) {
        return areSameTypes(t, s, false);
    }
    
    public static boolean areSameTypes(final List<? extends TypeReference> t, final List<? extends TypeReference> s, final boolean strict) {
        if (t.size() != s.size()) {
            return false;
        }
        for (int i = 0, n = t.size(); i < n; ++i) {
            if (!isSameType((TypeReference)t.get(i), (TypeReference)s.get(i), strict)) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean isCaptureOf(final TypeReference t, final TypeReference s) {
        return isSameWildcard(t, s);
    }
    
    private static boolean isSameWildcard(final TypeReference t, final TypeReference s) {
        VerifyArgument.notNull(t, "t");
        VerifyArgument.notNull(s, "s");
        if (!t.isWildcardType() || !s.isWildcardType()) {
            return false;
        }
        if (t.isUnbounded()) {
            return s.isUnbounded();
        }
        if (t.hasSuperBound()) {
            return s.hasSuperBound() && isSameType(t.getSuperBound(), s.getSuperBound());
        }
        return s.hasExtendsBound() && isSameType(t.getExtendsBound(), s.getExtendsBound());
    }
    
    private static List<? extends TypeReference> getTypeArguments(final TypeReference t) {
        if (t instanceof IGenericInstance) {
            return ((IGenericInstance)t).getTypeArguments();
        }
        if (t.isGenericType()) {
            return t.getGenericParameters();
        }
        return Collections.emptyList();
    }
    
    private static boolean containsType(final List<? extends TypeReference> t, final List<? extends TypeReference> s) {
        if (t.size() != s.size()) {
            return false;
        }
        if (t.isEmpty()) {
            return true;
        }
        for (int i = 0, n = t.size(); i < n; ++i) {
            if (!containsType((TypeReference)t.get(i), (TypeReference)s.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean containsTypeEquivalent(final TypeReference t, final TypeReference s) {
        return s == t || (containsType(t, s) && containsType(s, t));
    }
    
    private static boolean containsTypeEquivalent(final List<? extends TypeReference> t, final List<? extends TypeReference> s) {
        if (t.size() != s.size()) {
            return false;
        }
        for (int i = 0, n = t.size(); i < n; ++i) {
            if (!containsTypeEquivalent((TypeReference)t.get(i), (TypeReference)s.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    private static boolean containsTypeRecursive(final TypeReference t, final TypeReference s) {
        final HashSet<Pair<TypeReference, TypeReference>> cache = MetadataHelper.CONTAINS_TYPE_CACHE.get();
        final Pair<TypeReference, TypeReference> pair = new Pair<TypeReference, TypeReference>(t, s);
        if (cache.add(pair)) {
            try {
                return containsType(getTypeArguments(t), getTypeArguments(s));
            }
            finally {
                cache.remove(pair);
            }
        }
        return containsType(getTypeArguments(t), getTypeArguments(rewriteSupers(s)));
    }
    
    private static TypeReference arraySuperType(final TypeReference t) {
        final TypeDefinition resolved = t.resolve();
        if (resolved != null) {
            final IMetadataResolver resolver = resolved.getResolver();
            final TypeReference cloneable = resolver.lookupType("java/lang/Cloneable");
            final TypeReference serializable = resolver.lookupType("java/io/Serializable");
            if (cloneable != null) {
                if (serializable != null) {
                    return new CompoundTypeReference(null, ArrayUtilities.asUnmodifiableList(cloneable, serializable));
                }
                return cloneable;
            }
            else if (serializable != null) {
                return serializable;
            }
        }
        return BuiltinTypes.Object;
    }
    
    public static boolean isRawType(final TypeReference t) {
        if (t == null) {
            return false;
        }
        if (t instanceof RawType) {
            return true;
        }
        if (t.isGenericType()) {
            return false;
        }
        final TypeReference r = t.resolve();
        return r != null && r.isGenericType();
    }
    
    public static int getUnboundGenericParameterCount(final TypeReference t) {
        if (t == null || t instanceof RawType || !t.isGenericType()) {
            return 0;
        }
        final List<GenericParameter> genericParameters = t.getGenericParameters();
        if (t.isGenericDefinition()) {
            return genericParameters.size();
        }
        final IGenericParameterProvider genericDefinition = ((IGenericInstance)t).getGenericDefinition();
        if (!genericDefinition.isGenericDefinition()) {
            return 0;
        }
        final List<TypeReference> typeArguments = ((IGenericInstance)t).getTypeArguments();
        assert genericParameters.size() == typeArguments.size();
        int count = 0;
        for (int i = 0; i < genericParameters.size(); ++i) {
            final GenericParameter genericParameter = genericParameters.get(i);
            final TypeReference typeArgument = typeArguments.get(i);
            if (isSameType(genericParameter, typeArgument, true)) {
                ++count;
            }
        }
        return count;
    }
    
    public static List<TypeReference> eraseRecursive(final List<TypeReference> types) {
        ArrayList<TypeReference> result = null;
        for (int i = 0, n = types.size(); i < n; ++i) {
            final TypeReference type = types.get(i);
            final TypeReference erased = eraseRecursive(type);
            if (result != null) {
                result.set(i, erased);
            }
            else if (type != erased) {
                result = new ArrayList<TypeReference>(types);
                result.set(i, erased);
            }
        }
        return (result != null) ? result : types;
    }
    
    public static TypeReference eraseRecursive(final TypeReference type) {
        return erase(type, true);
    }
    
    private static boolean eraseNotNeeded(final TypeReference type) {
        return type == null || type instanceof RawType || type.isPrimitive() || StringUtilities.equals(type.getInternalName(), CommonTypeReferences.String.getInternalName());
    }
    
    public static TypeReference erase(final TypeReference type) {
        return erase(type, false);
    }
    
    public static TypeReference erase(final TypeReference type, final boolean recurse) {
        if (eraseNotNeeded(type)) {
            return type;
        }
        return type.accept(MetadataHelper.ERASE_VISITOR, recurse);
    }
    
    public static MethodReference erase(final MethodReference method) {
        if (method != null) {
            MethodReference baseMethod = method;
            final MethodDefinition resolvedMethod = baseMethod.resolve();
            if (resolvedMethod != null) {
                baseMethod = resolvedMethod;
            }
            else if (baseMethod instanceof IGenericInstance) {
                baseMethod = (MethodReference)((IGenericInstance)baseMethod).getGenericDefinition();
            }
            if (baseMethod != null) {
                return new RawMethod(baseMethod);
            }
        }
        return method;
    }
    
    private static TypeReference classBound(final TypeReference t) {
        return t;
    }
    
    public static boolean isOverride(final MethodDefinition method, final MethodReference ancestorMethod) {
        final MethodDefinition resolvedAncestor = ancestorMethod.resolve();
        if (resolvedAncestor == null || resolvedAncestor.isFinal() || resolvedAncestor.isPrivate() || resolvedAncestor.isStatic()) {
            return false;
        }
        final int modifiers = method.getModifiers() & 0x7;
        final int ancestorModifiers = resolvedAncestor.getModifiers() & 0x7;
        if (modifiers != ancestorModifiers) {
            return false;
        }
        if (!StringUtilities.equals(method.getName(), ancestorMethod.getName())) {
            return false;
        }
        if (method.getDeclaringType().isInterface()) {
            return false;
        }
        final MethodDefinition resolved = method.resolve();
        final TypeReference declaringType = erase((resolved != null) ? resolved.getDeclaringType() : method.getDeclaringType());
        final TypeReference ancestorDeclaringType = erase(resolvedAncestor.getDeclaringType());
        if (isSameType(declaringType, ancestorDeclaringType)) {
            return false;
        }
        if (StringUtilities.equals(method.getErasedSignature(), ancestorMethod.getErasedSignature())) {
            return true;
        }
        if (!isSubType(declaringType, ancestorDeclaringType)) {
            return false;
        }
        final List<ParameterDefinition> parameters = method.getParameters();
        final List<ParameterDefinition> ancestorParameters = ancestorMethod.getParameters();
        if (parameters.size() != ancestorParameters.size()) {
            return false;
        }
        final TypeReference ancestorReturnType = erase(ancestorMethod.getReturnType());
        final TypeReference baseReturnType = erase(method.getReturnType());
        if (!isAssignableFrom(ancestorReturnType, baseReturnType)) {
            return false;
        }
        for (int i = 0, n = ancestorParameters.size(); i < n; ++i) {
            final TypeReference parameterType = erase(parameters.get(i).getParameterType());
            final TypeReference ancestorParameterType = erase(ancestorParameters.get(i).getParameterType());
            if (!isSameType(parameterType, ancestorParameterType, false)) {
                return false;
            }
        }
        return true;
    }
    
    static List<ParameterDefinition> copyParameters(final List<ParameterDefinition> parameters) {
        final List<ParameterDefinition> newParameters = new ArrayList<ParameterDefinition>();
        for (final ParameterDefinition p : parameters) {
            if (p.hasName()) {
                newParameters.add(new ParameterDefinition(p.getSlot(), p.getName(), p.getParameterType()));
            }
            else {
                newParameters.add(new ParameterDefinition(p.getSlot(), p.getParameterType()));
            }
        }
        return newParameters;
    }
    
    static /* synthetic */ boolean access$0(final TypeReference param_0, final TypeReference param_1) {
        return containsTypeRecursive(param_0, param_1);
    }
    
    static /* synthetic */ boolean access$1(final TypeReference param_0, final TypeReference param_1) {
        return isSameWildcard(param_0, param_1);
    }
    
    static /* synthetic */ boolean access$2(final TypeReference param_0, final TypeReference param_1) {
        return isCaptureOf(param_0, param_1);
    }
    
    static /* synthetic */ TypeReference access$3(final TypeReference param_0) {
        return arraySuperType(param_0);
    }
    
    static /* synthetic */ TypeReference access$4(final TypeReference param_0) {
        return classBound(param_0);
    }
    
    static /* synthetic */ List access$5(final TypeReference param_0) {
        return getTypeArguments(param_0);
    }
    
    static /* synthetic */ ThreadLocal access$6() {
        return MetadataHelper.ADAPT_CACHE;
    }
    
    static /* synthetic */ boolean access$7(final TypeReference param_0, final TypeReference param_1) {
        return containsTypeEquivalent(param_0, param_1);
    }
    
    static /* synthetic */ boolean access$8(final List param_0, final List param_1) {
        return containsTypeEquivalent(param_0, param_1);
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType() {
        final int[] loc_0 = MetadataHelper.$SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[ConversionType.values().length];
        try {
            loc_1[ConversionType.EXPLICIT.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[ConversionType.EXPLICIT_TO_UNBOXED.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[ConversionType.IDENTITY.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[ConversionType.IMPLICIT.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[ConversionType.NONE.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_6) {}
        return MetadataHelper.$SWITCH_TABLE$com$strobel$assembler$metadata$ConversionType = loc_1;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = MetadataHelper.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        if (loc_0 != null) {
            return loc_0;
        }
        final int[] loc_1 = new int[JvmType.values().length];
        try {
            loc_1[JvmType.Array.ordinal()] = 10;
        }
        catch (NoSuchFieldError loc_2) {}
        try {
            loc_1[JvmType.Boolean.ordinal()] = 1;
        }
        catch (NoSuchFieldError loc_3) {}
        try {
            loc_1[JvmType.Byte.ordinal()] = 2;
        }
        catch (NoSuchFieldError loc_4) {}
        try {
            loc_1[JvmType.Character.ordinal()] = 3;
        }
        catch (NoSuchFieldError loc_5) {}
        try {
            loc_1[JvmType.Double.ordinal()] = 8;
        }
        catch (NoSuchFieldError loc_6) {}
        try {
            loc_1[JvmType.Float.ordinal()] = 7;
        }
        catch (NoSuchFieldError loc_7) {}
        try {
            loc_1[JvmType.Integer.ordinal()] = 5;
        }
        catch (NoSuchFieldError loc_8) {}
        try {
            loc_1[JvmType.Long.ordinal()] = 6;
        }
        catch (NoSuchFieldError loc_9) {}
        try {
            loc_1[JvmType.Object.ordinal()] = 9;
        }
        catch (NoSuchFieldError loc_10) {}
        try {
            loc_1[JvmType.Short.ordinal()] = 4;
        }
        catch (NoSuchFieldError loc_11) {}
        try {
            loc_1[JvmType.TypeVariable.ordinal()] = 11;
        }
        catch (NoSuchFieldError loc_12) {}
        try {
            loc_1[JvmType.Void.ordinal()] = 13;
        }
        catch (NoSuchFieldError loc_13) {}
        try {
            loc_1[JvmType.Wildcard.ordinal()] = 12;
        }
        catch (NoSuchFieldError loc_14) {}
        return MetadataHelper.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
    
    private static final class Adapter extends DefaultTypeVisitor<TypeReference, Void>
    {
        final ListBuffer<TypeReference> from;
        final ListBuffer<TypeReference> to;
        final Map<TypeReference, TypeReference> mapping;
        
        private Adapter() {
            super();
            this.from = ListBuffer.lb();
            this.to = ListBuffer.lb();
            this.mapping = new HashMap<TypeReference, TypeReference>();
        }
        
        private void adaptRecursive(final List<? extends TypeReference> source, final List<? extends TypeReference> target) {
            if (source.size() == target.size()) {
                for (int i = 0, n = source.size(); i < n; ++i) {
                    this.adaptRecursive((TypeReference)source.get(i), (TypeReference)target.get(i));
                }
            }
        }
        
        @Override
        public Void visitClassType(final TypeReference source, final TypeReference target) {
            this.adaptRecursive(MetadataHelper.access$5(source), MetadataHelper.access$5(target));
            return null;
        }
        
        @Override
        public Void visitParameterizedType(final TypeReference source, final TypeReference target) {
            this.adaptRecursive(MetadataHelper.access$5(source), MetadataHelper.access$5(target));
            return null;
        }
        
        private void adaptRecursive(final TypeReference source, final TypeReference target) {
            final HashSet<Pair<TypeReference, TypeReference>> cache = MetadataHelper.access$6().get();
            final Pair<TypeReference, TypeReference> pair = Pair.create(source, target);
            if (cache.add(pair)) {
                try {
                    ((DefaultTypeVisitor<TypeReference, Object>)this).visit(source, target);
                }
                finally {
                    cache.remove(pair);
                }
                cache.remove(pair);
            }
        }
        
        @Override
        public Void visitArrayType(final ArrayType source, final TypeReference target) {
            if (target.isArray()) {
                this.adaptRecursive(MetadataHelper.getElementType(source), MetadataHelper.getElementType(target));
            }
            return null;
        }
        
        @Override
        public Void visitWildcard(final WildcardType source, final TypeReference target) {
            if (source.hasExtendsBound()) {
                this.adaptRecursive(MetadataHelper.getUpperBound(source), MetadataHelper.getUpperBound(target));
            }
            else if (source.hasSuperBound()) {
                this.adaptRecursive(MetadataHelper.getLowerBound(source), MetadataHelper.getLowerBound(target));
            }
            return null;
        }
        
        @Override
        public Void visitGenericParameter(final GenericParameter source, final TypeReference target) {
            TypeReference value = this.mapping.get(source);
            if (value != null) {
                if (value.hasSuperBound() && target.hasSuperBound()) {
                    value = (MetadataHelper.isSubType(MetadataHelper.getLowerBound(value), MetadataHelper.getLowerBound(target)) ? target : value);
                }
                else if (value.hasExtendsBound() && target.hasExtendsBound()) {
                    value = (MetadataHelper.isSubType(MetadataHelper.getUpperBound(value), MetadataHelper.getUpperBound(target)) ? value : target);
                }
                else if ((!value.isWildcardType() || !value.isUnbounded()) && !MetadataHelper.isSameType(value, target)) {
                    throw new AdaptFailure();
                }
            }
            else {
                value = target;
                this.from.append(source);
                this.to.append(target);
            }
            this.mapping.put(source, value);
            return null;
        }
    }
    
    abstract static class SameTypeVisitor extends TypeRelation
    {
        abstract boolean areSameGenericParameters(final GenericParameter param_0, final GenericParameter param_1);
        
        protected abstract boolean containsTypes(final List<? extends TypeReference> param_0, final List<? extends TypeReference> param_1);
        
        @Override
        public Boolean visit(final TypeReference t, final TypeReference s) {
            if (t == null) {
                if (s == null) {
                    return true;
                }
                return false;
            }
            else {
                if (s == null) {
                    return false;
                }
                return t.accept((TypeMetadataVisitor<TypeReference, Boolean>)this, s);
            }
        }
        
        @Override
        public Boolean visitType(final TypeReference t, final TypeReference s) {
            return Boolean.FALSE;
        }
        
        @Override
        public Boolean visitArrayType(final ArrayType t, final TypeReference s) {
            if (s.isArray() && MetadataHelper.access$7(MetadataHelper.getElementType(t), MetadataHelper.getElementType(s))) {
                return true;
            }
            return false;
        }
        
        @Override
        public Boolean visitBottomType(final TypeReference t, final TypeReference s) {
            if (t == s) {
                return true;
            }
            return false;
        }
        
        @Override
        public Boolean visitClassType(final TypeReference t, final TypeReference s) {
            if (t == s) {
                return true;
            }
            if (!(t instanceof RawType) && MetadataHelper.isRawType(t)) {
                final TypeDefinition tResolved = t.resolve();
                if (tResolved != null) {
                    return this.visitClassType(tResolved, s);
                }
            }
            if (!(s instanceof RawType) && MetadataHelper.isRawType(s)) {
                final TypeDefinition sResolved = s.resolve();
                if (sResolved != null) {
                    return this.visitClassType(t, sResolved);
                }
            }
            if (t.isGenericDefinition()) {
                if (!s.isGenericDefinition()) {
                    return false;
                }
                if (StringUtilities.equals(t.getInternalName(), s.getInternalName()) && this.visit(t.getDeclaringType(), s.getDeclaringType())) {
                    return true;
                }
                return false;
            }
            else {
                if (s.getSimpleType() == JvmType.Object && StringUtilities.equals(t.getInternalName(), s.getInternalName())) {
                    return true;
                }
                return false;
            }
        }
        
        @Override
        public Boolean visitCompoundType(final CompoundTypeReference t, final TypeReference s) {
            if (!s.isCompoundType()) {
                return false;
            }
            if (!this.visit(MetadataHelper.getSuperType(t), MetadataHelper.getSuperType(s))) {
                return false;
            }
            final HashSet<TypeReference> set = new HashSet<TypeReference>();
            for (final TypeReference i : MetadataHelper.getInterfaces(t)) {
                set.add(i);
            }
            for (final TypeReference i : MetadataHelper.getInterfaces(s)) {
                if (!set.remove(i)) {
                    return false;
                }
            }
            return set.isEmpty();
        }
        
        @Override
        public Boolean visitGenericParameter(final GenericParameter t, final TypeReference s) {
            if (s instanceof GenericParameter) {
                return this.areSameGenericParameters(t, (GenericParameter)s);
            }
            if (s.hasSuperBound() && !s.hasExtendsBound() && this.visit(t, MetadataHelper.getUpperBound(s))) {
                return true;
            }
            return false;
        }
        
        @Override
        public Boolean visitNullType(final TypeReference t, final TypeReference s) {
            if (t == s) {
                return true;
            }
            return false;
        }
        
        @Override
        public Boolean visitParameterizedType(final TypeReference t, final TypeReference s) {
            return this.visitClassType(t, s);
        }
        
        @Override
        public Boolean visitPrimitiveType(final PrimitiveType t, final TypeReference s) {
            if (t.getSimpleType() == s.getSimpleType()) {
                return true;
            }
            return false;
        }
        
        @Override
        public Boolean visitRawType(final RawType t, final TypeReference s) {
            if (s.getSimpleType() == JvmType.Object && !s.isGenericType() && StringUtilities.equals(t.getInternalName(), s.getInternalName())) {
                return true;
            }
            return false;
        }
        
        @Override
        public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
            if (s.isWildcardType()) {
                if (t.isUnbounded()) {
                    return s.isUnbounded();
                }
                if (t.hasExtendsBound()) {
                    if (s.hasExtendsBound() && this.visit(MetadataHelper.getUpperBound(t), MetadataHelper.getUpperBound(s))) {
                        return true;
                    }
                    return false;
                }
                else if (t.hasSuperBound()) {
                    if (s.hasSuperBound() && this.visit(MetadataHelper.getLowerBound(t), MetadataHelper.getLowerBound(s))) {
                        return true;
                    }
                    return false;
                }
            }
            return Boolean.FALSE;
        }
    }
    
    public static class AdaptFailure extends RuntimeException
    {
        static final long serialVersionUID = -7490231548272701566L;
    }
    
    static final class LooseSameTypeVisitor extends SameTypeVisitor
    {
        @Override
        boolean areSameGenericParameters(final GenericParameter gp1, final GenericParameter gp2) {
            if (gp1 == gp2) {
                return true;
            }
            if (gp1 == null || gp2 == null) {
                return false;
            }
            if (!StringUtilities.equals(gp1.getName(), gp2.getName())) {
                return false;
            }
            final IGenericParameterProvider owner1 = gp1.getOwner();
            final IGenericParameterProvider owner2 = gp2.getOwner();
            if (owner1.getGenericParameters().indexOf(gp1) != owner1.getGenericParameters().indexOf(gp2)) {
                return false;
            }
            if (owner1 == owner2) {
                return true;
            }
            if (owner1 instanceof TypeReference) {
                return owner2 instanceof TypeReference && StringUtilities.equals(((TypeReference)owner1).getInternalName(), ((TypeReference)owner2).getInternalName());
            }
            return owner1 instanceof MethodReference && owner2 instanceof MethodReference && StringUtilities.equals(((MethodReference)owner1).getFullName(), ((MethodReference)owner2).getFullName()) && StringUtilities.equals(((MethodReference)owner1).getErasedSignature(), ((MethodReference)owner2).getErasedSignature());
        }
        
        @Override
        protected boolean containsTypes(final List<? extends TypeReference> t1, final List<? extends TypeReference> t2) {
            return MetadataHelper.access$8(t1, t2);
        }
    }
    
    static final class StrictSameTypeVisitor extends SameTypeVisitor
    {
        @Override
        boolean areSameGenericParameters(final GenericParameter gp1, final GenericParameter gp2) {
            if (gp1 == gp2) {
                return true;
            }
            if (gp1 == null || gp2 == null) {
                return false;
            }
            if (!StringUtilities.equals(gp1.getName(), gp2.getName())) {
                return false;
            }
            final IGenericParameterProvider owner1 = gp1.getOwner();
            final IGenericParameterProvider owner2 = gp2.getOwner();
            if (owner1 == null || owner2 == null) {
                if (owner1 != owner2) {
                    return false;
                }
            }
            else if (CollectionUtilities.indexOfByIdentity(owner1.getGenericParameters(), gp1) != CollectionUtilities.indexOfByIdentity(owner2.getGenericParameters(), gp2)) {
                return false;
            }
            if (owner1 == owner2) {
                return true;
            }
            if (owner1 instanceof TypeReference) {
                return owner2 instanceof TypeReference && StringUtilities.equals(gp1.getName(), gp2.getName()) && StringUtilities.equals(((TypeReference)owner1).getInternalName(), ((TypeReference)owner2).getInternalName());
            }
            return owner1 instanceof MethodReference && owner2 instanceof MethodReference && StringUtilities.equals(gp1.getName(), gp2.getName()) && StringUtilities.equals(((MethodReference)owner1).getFullName(), ((MethodReference)owner2).getFullName()) && StringUtilities.equals(((MethodReference)owner1).getErasedSignature(), ((MethodReference)owner2).getErasedSignature());
        }
        
        @Override
        protected boolean containsTypes(final List<? extends TypeReference> t1, final List<? extends TypeReference> t2) {
            return MetadataHelper.areSameTypes(t1, t2, true);
        }
        
        @Override
        public Boolean visitWildcard(final WildcardType t, final TypeReference s) {
            if (!s.isWildcardType()) {
                return false;
            }
            if (t.isUnbounded()) {
                return s.isUnbounded();
            }
            if (t.hasExtendsBound()) {
                if (s.hasExtendsBound() && MetadataHelper.isSameType(t.getExtendsBound(), s.getExtendsBound())) {
                    return true;
                }
                return false;
            }
            else {
                if (s.hasSuperBound() && MetadataHelper.isSameType(t.getSuperBound(), s.getSuperBound())) {
                    return true;
                }
                return false;
            }
        }
    }
}
