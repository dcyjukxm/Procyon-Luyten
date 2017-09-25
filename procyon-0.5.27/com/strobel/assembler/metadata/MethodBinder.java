package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;

public final class MethodBinder
{
    public static BindResult selectMethod(final List<? extends MethodReference> matches, final List<TypeReference> types) {
        VerifyArgument.notNull(matches, "matches");
        VerifyArgument.notNull(types, "types");
        if (types.isEmpty()) {
            return null;
        }
        final int argumentCount = types.size();
        final MethodReference[] candidates = matches.toArray(new MethodReference[matches.size()]);
        for (int i = 0; i < candidates.length; ++i) {
            final MethodReference candidate = candidates[i];
            if (candidate.isGenericMethod()) {
                final Map<TypeReference, TypeReference> mappings = new HashMap<TypeReference, TypeReference>();
                final List<ParameterDefinition> parameters = candidate.getParameters();
                for (int j = 0, n = Math.min(argumentCount, parameters.size()); j < n; ++j) {
                    final ParameterDefinition p = parameters.get(j);
                    final TypeReference pType = p.getParameterType();
                    if (pType.containsGenericParameters()) {
                        new AddMappingsForArgumentVisitor(types.get(j)).visit(pType, mappings);
                    }
                }
                candidates[i] = TypeSubstitutionVisitor.instance().visitMethod(candidate, mappings);
            }
        }
        int currentIndex = 0;
        for (int k = 0, n2 = candidates.length; k < n2; ++k) {
            final MethodReference candidate2 = candidates[k];
            final MethodDefinition resolved = candidate2.resolve();
            final List<ParameterDefinition> parameters2 = candidate2.getParameters();
            final int parameterCount = parameters2.size();
            final boolean isVarArgs = resolved != null && resolved.isVarArgs();
            if (parameterCount == types.size() || isVarArgs) {
                int stop;
                for (stop = 0; stop < Math.min(parameterCount, types.size()); ++stop) {
                    final TypeReference parameterType = parameters2.get(stop).getParameterType();
                    if (!MetadataHelper.isSameType(parameterType, types.get(stop), false)) {
                        if (!MetadataHelper.isSameType(parameterType, BuiltinTypes.Object, false)) {
                            if (!MetadataHelper.isAssignableFrom(parameterType, types.get(stop))) {
                                if (!isVarArgs) {
                                    break;
                                }
                                if (stop != parameterCount - 1) {
                                    break;
                                }
                                if (!MetadataHelper.isAssignableFrom(parameterType.getElementType(), types.get(stop))) {
                                    break;
                                }
                            }
                        }
                    }
                }
                if (stop == parameterCount || (stop == parameterCount - 1 && isVarArgs)) {
                    candidates[currentIndex++] = candidate2;
                }
            }
        }
        if (currentIndex == 0) {
            return BindResult.FAILURE;
        }
        if (currentIndex == 1) {
            return new BindResult(false, candidates[0], null);
        }
        int currentMin = 0;
        boolean ambiguous = false;
        final int[] parameterOrder = new int[types.size()];
        for (int l = 0, n3 = types.size(); l < n3; ++l) {
            parameterOrder[l] = l;
        }
        for (int l = 1; l < currentIndex; ++l) {
            final MethodReference m1 = candidates[currentMin];
            final MethodReference m2 = candidates[l];
            final MethodDefinition r1 = m1.resolve();
            final MethodDefinition r2 = m2.resolve();
            TypeReference varArgType1;
            if (r1 != null && r1.isVarArgs()) {
                final List<ParameterDefinition> p2 = m1.getParameters();
                varArgType1 = p2.get(p2.size() - 1).getParameterType().getElementType();
            }
            else {
                varArgType1 = null;
            }
            TypeReference varArgType2;
            if (r2 != null && r2.isVarArgs()) {
                final List<ParameterDefinition> p3 = m2.getParameters();
                varArgType2 = p3.get(p3.size() - 1).getParameterType().getElementType();
            }
            else {
                varArgType2 = null;
            }
            final int newMin = findMostSpecificMethod(m1, parameterOrder, varArgType1, candidates[l], parameterOrder, varArgType2, types, null);
            if (newMin == 0) {
                ambiguous = true;
            }
            else if (newMin == 2) {
                ambiguous = false;
                currentMin = l;
            }
        }
        if (ambiguous) {
            return new BindResult(true, candidates[currentMin], null);
        }
        return new BindResult(false, candidates[currentMin], null);
    }
    
    private static int findMostSpecificMethod(final MethodReference m1, final int[] varArgOrder1, final TypeReference varArgArrayType1, final MethodReference m2, final int[] varArgOrder2, final TypeReference varArgArrayType2, final List<TypeReference> types, final Object[] args) {
        int result = findMostSpecific(m1.getParameters(), varArgOrder1, null, m2.getParameters(), varArgOrder2, null, types, args, false);
        if (result == 0) {
            result = findMostSpecific(m1.getParameters(), varArgOrder1, null, m2.getParameters(), varArgOrder2, null, types, args, true);
        }
        if (result == 0) {
            result = findMostSpecific(m1.getParameters(), varArgOrder1, varArgArrayType1, m2.getParameters(), varArgOrder2, varArgArrayType2, types, args, true);
        }
        if (result != 0) {
            return result;
        }
        if (!compareMethodSignatureAndName(m1, m2)) {
            return 0;
        }
        final int hierarchyDepth1 = getHierarchyDepth(m1.getDeclaringType());
        final int hierarchyDepth2 = getHierarchyDepth(m2.getDeclaringType());
        if (hierarchyDepth1 == hierarchyDepth2) {
            return 0;
        }
        if (hierarchyDepth1 < hierarchyDepth2) {
            return 2;
        }
        return 1;
    }
    
    private static int findMostSpecific(final List<ParameterDefinition> p1, final int[] varArgOrder1, final TypeReference varArgArrayType1, final List<ParameterDefinition> p2, final int[] varArgOrder2, final TypeReference varArgArrayType2, final List<TypeReference> types, final Object[] args, final boolean allowAutoBoxing) {
        if (varArgArrayType1 != null && varArgArrayType2 == null && types.size() != p1.size()) {
            return 2;
        }
        if (varArgArrayType2 != null && varArgArrayType1 == null && types.size() != p2.size()) {
            return 1;
        }
        boolean p1Less = false;
        boolean p2Less = false;
        for (int max = (varArgArrayType1 != null) ? types.size() : Math.min(p1.size(), p2.size()), i = 0; i < max; ++i) {
            if (args == null) {
                TypeReference c1;
                if (varArgArrayType1 != null && varArgOrder1[i] >= p1.size() - 1) {
                    c1 = varArgArrayType1;
                }
                else {
                    c1 = p1.get(varArgOrder1[i]).getParameterType();
                }
                TypeReference c2;
                if (varArgArrayType2 != null && varArgOrder2[i] >= p2.size() - 1) {
                    c2 = varArgArrayType2;
                }
                else {
                    c2 = p2.get(varArgOrder2[i]).getParameterType();
                }
                if (c1 != c2) {
                    switch (findMostSpecificType(c1, c2, types.get(i), allowAutoBoxing)) {
                        case 1: {
                            p1Less = true;
                            break;
                        }
                        case 2: {
                            p2Less = true;
                            break;
                        }
                    }
                }
            }
        }
        if (p1Less == p2Less) {
            if (!p1Less && args != null) {
                if (p1.size() > p2.size()) {
                    return 1;
                }
                if (p2.size() > p1.size()) {
                    return 2;
                }
            }
            return 0;
        }
        return p1Less ? 1 : 2;
    }
    
    private static int findMostSpecificType(final TypeReference c1, final TypeReference c2, final TypeReference t, final boolean allowAutoBoxing) {
        if (MetadataHelper.isSameType(c1, c2, false)) {
            return 0;
        }
        if (MetadataHelper.isSameType(c1, t, false)) {
            return 1;
        }
        if (MetadataHelper.isSameType(c2, t, false)) {
            return 2;
        }
        final boolean c1FromT = (allowAutoBoxing || c1.isPrimitive() == t.isPrimitive()) && MetadataHelper.isAssignableFrom(c1, t);
        final boolean c2FromT = (allowAutoBoxing || c2.isPrimitive() == t.isPrimitive()) && MetadataHelper.isAssignableFrom(c2, t);
        if (c1FromT != c2FromT) {
            return c1FromT ? 1 : 2;
        }
        boolean c1FromC2;
        boolean c2FromC1;
        if (allowAutoBoxing || c1.isPrimitive() == c2.isPrimitive()) {
            c1FromC2 = MetadataHelper.isAssignableFrom(c1, c2);
            c2FromC1 = MetadataHelper.isAssignableFrom(c2, c1);
        }
        else {
            c1FromC2 = false;
            c2FromC1 = false;
        }
        if (c1FromC2 != c2FromC1) {
            return c1FromC2 ? 2 : 1;
        }
        if (!t.isPrimitive() && c1.isPrimitive() != c2.isPrimitive()) {
            return c1.isPrimitive() ? 2 : 1;
        }
        return 0;
    }
    
    private static boolean compareMethodSignatureAndName(final MethodReference m1, final MethodReference m2) {
        final List<ParameterDefinition> p1 = m1.getParameters();
        final List<ParameterDefinition> p2 = m2.getParameters();
        if (p1.size() != p2.size()) {
            return false;
        }
        for (int i = 0, n = p1.size(); i < n; ++i) {
            if (!MetadataHelper.isSameType(p1.get(i).getParameterType(), p2.get(i).getParameterType(), false)) {
                return false;
            }
        }
        return true;
    }
    
    private static int getHierarchyDepth(final TypeReference t) {
        int depth = 0;
        TypeReference currentType = t;
        do {
            ++depth;
            currentType = MetadataHelper.getBaseType(currentType);
        } while (currentType != null);
        return depth;
    }
    
    private static final class AddMappingsForArgumentVisitor extends DefaultTypeVisitor<Map<TypeReference, TypeReference>, Void>
    {
        private TypeReference argumentType;
        private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
        
        AddMappingsForArgumentVisitor(final TypeReference argumentType) {
            super();
            this.argumentType = VerifyArgument.notNull(argumentType, "argumentType");
        }
        
        @Override
        public Void visit(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            final TypeReference a = this.argumentType;
            t.accept((TypeMetadataVisitor<Map<TypeReference, TypeReference>, Object>)this, map);
            this.argumentType = a;
            return null;
        }
        
        @Override
        public Void visitArrayType(final ArrayType t, final Map<TypeReference, TypeReference> map) {
            final TypeReference a = this.argumentType;
            if (a.isArray() && t.isArray()) {
                this.argumentType = a.getElementType();
                this.visit(t.getElementType(), map);
            }
            return null;
        }
        
        @Override
        public Void visitGenericParameter(final GenericParameter t, final Map<TypeReference, TypeReference> map) {
            if (MetadataResolver.areEquivalent(this.argumentType, t)) {
                return null;
            }
            final TypeReference existingMapping = map.get(t);
            TypeReference mappedType = this.argumentType;
            mappedType = ensureReferenceType(mappedType);
            if (existingMapping == null) {
                if (!(mappedType instanceof RawType) && MetadataHelper.isRawType(mappedType)) {
                    final TypeReference bound = MetadataHelper.getUpperBound(t);
                    final TypeReference asSuper = MetadataHelper.asSuper(mappedType, bound);
                    if (asSuper != null) {
                        if (MetadataHelper.isSameType(MetadataHelper.getUpperBound(t), asSuper)) {
                            return null;
                        }
                        mappedType = asSuper;
                    }
                    else {
                        mappedType = MetadataHelper.erase(mappedType);
                    }
                }
                map.put(t, mappedType);
            }
            else if (!MetadataHelper.isSubType(this.argumentType, existingMapping)) {
                TypeReference commonSuperType = MetadataHelper.asSuper(mappedType, existingMapping);
                if (commonSuperType == null) {
                    commonSuperType = MetadataHelper.asSuper(existingMapping, mappedType);
                }
                if (commonSuperType == null) {
                    commonSuperType = MetadataHelper.findCommonSuperType(existingMapping, mappedType);
                }
                map.put(t, commonSuperType);
            }
            return null;
        }
        
        @Override
        public Void visitWildcard(final WildcardType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitCompoundType(final CompoundTypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitParameterizedType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            final TypeReference r = MetadataHelper.asSuper(t.getUnderlyingType(), this.argumentType);
            final TypeReference s = MetadataHelper.asSubType(this.argumentType, (r != null) ? r : t.getUnderlyingType());
            if (s != null && s instanceof IGenericInstance) {
                final List<TypeReference> tArgs = ((IGenericInstance)t).getTypeArguments();
                final List<TypeReference> sArgs = ((IGenericInstance)s).getTypeArguments();
                if (tArgs.size() == sArgs.size()) {
                    for (int i = 0, n = tArgs.size(); i < n; ++i) {
                        this.argumentType = sArgs.get(i);
                        this.visit(tArgs.get(i), map);
                    }
                }
            }
            return null;
        }
        
        @Override
        public Void visitPrimitiveType(final PrimitiveType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitClassType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitNullType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitBottomType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        @Override
        public Void visitRawType(final RawType t, final Map<TypeReference, TypeReference> map) {
            return null;
        }
        
        private static TypeReference ensureReferenceType(final TypeReference mappedType) {
            if (mappedType == null) {
                return null;
            }
            if (mappedType.isPrimitive()) {
                switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[mappedType.getSimpleType().ordinal()]) {
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
                }
            }
            return mappedType;
        }
        
        static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
            final int[] loc_0 = AddMappingsForArgumentVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
            return AddMappingsForArgumentVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
        }
    }
    
    public static class BindResult
    {
        public static final BindResult FAILURE;
        public static final BindResult AMBIGUOUS;
        private final boolean _ambiguous;
        private final MethodReference _method;
        
        static {
            FAILURE = new BindResult(false, null);
            AMBIGUOUS = new BindResult(true, null);
        }
        
        private BindResult(final boolean ambiguous, final MethodReference method) {
            super();
            this._ambiguous = ambiguous;
            this._method = method;
        }
        
        public final boolean isFailure() {
            return this._method == null;
        }
        
        public final boolean isAmbiguous() {
            return this._ambiguous;
        }
        
        public final MethodReference getMethod() {
            return this._method;
        }
    }
}
