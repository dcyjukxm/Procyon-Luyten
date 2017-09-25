package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;

public final class TypeSubstitutionVisitor extends DefaultTypeVisitor<Map<TypeReference, TypeReference>, TypeReference> implements MethodMetadataVisitor<Map<TypeReference, TypeReference>, MethodReference>, FieldMetadataVisitor<Map<TypeReference, TypeReference>, FieldReference>
{
    private static final TypeSubstitutionVisitor INSTANCE;
    private static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
    
    static {
        INSTANCE = new TypeSubstitutionVisitor();
    }
    
    public static TypeSubstitutionVisitor instance() {
        return TypeSubstitutionVisitor.INSTANCE;
    }
    
    @Override
    public TypeReference visit(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        if (map.isEmpty()) {
            return t;
        }
        return t.accept((TypeMetadataVisitor<Map<TypeReference, TypeReference>, TypeReference>)this, map);
    }
    
    @Override
    public TypeReference visitArrayType(final ArrayType t, final Map<TypeReference, TypeReference> map) {
        final TypeReference elementType = this.visit(t.getElementType(), map);
        if (elementType != null && elementType != t.getElementType()) {
            return elementType.makeArrayType();
        }
        return t;
    }
    
    @Override
    public TypeReference visitGenericParameter(final GenericParameter t, final Map<TypeReference, TypeReference> map) {
        TypeReference current;
        TypeReference mappedType;
        for (current = t; (mappedType = map.get(current)) != null && mappedType != current && map.get(mappedType) != current; current = mappedType) {}
        if (current == null) {
            return t;
        }
        if (current.isPrimitive()) {
            switch ($SWITCH_TABLE$com$strobel$assembler$metadata$JvmType()[current.getSimpleType().ordinal()]) {
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
        return current;
    }
    
    @Override
    public TypeReference visitWildcard(final WildcardType t, final Map<TypeReference, TypeReference> map) {
        if (t.isUnbounded()) {
            return t;
        }
        final TypeReference oldBound = t.hasExtendsBound() ? t.getExtendsBound() : t.getSuperBound();
        final TypeReference mapping = map.get(oldBound);
        if (MetadataResolver.areEquivalent(mapping, t)) {
            return t;
        }
        TypeReference newBound;
        for (newBound = this.visit(oldBound, map); newBound.isWildcardType(); newBound = (newBound.hasExtendsBound() ? newBound.getExtendsBound() : newBound.getSuperBound())) {
            if (newBound.isUnbounded()) {
                return newBound;
            }
        }
        if (oldBound != newBound) {
            return t.hasExtendsBound() ? WildcardType.makeExtends(newBound) : WildcardType.makeSuper(newBound);
        }
        return t;
    }
    
    @Override
    public TypeReference visitCompoundType(final CompoundTypeReference t, final Map<TypeReference, TypeReference> map) {
        final TypeReference oldBaseType = t.getBaseType();
        final TypeReference newBaseType = (oldBaseType != null) ? this.visit(oldBaseType, map) : null;
        TypeReference[] newInterfaces = null;
        boolean changed = newBaseType != oldBaseType;
        final List<TypeReference> oldInterfaces = t.getInterfaces();
        for (int i = 0; i < oldInterfaces.size(); ++i) {
            final TypeReference oldInterface = oldInterfaces.get(i);
            final TypeReference newInterface = this.visit(oldInterface, map);
            if (newInterfaces != null) {
                newInterfaces[i] = newInterface;
            }
            else if (oldInterface != newInterface) {
                newInterfaces = new TypeReference[oldInterfaces.size()];
                oldInterfaces.toArray(newInterfaces);
                newInterfaces[i] = newInterface;
                changed = true;
            }
        }
        if (changed) {
            return new CompoundTypeReference(newBaseType, (newInterfaces != null) ? ArrayUtilities.asUnmodifiableList(newInterfaces) : t.getInterfaces());
        }
        return t;
    }
    
    @Override
    public TypeReference visitParameterizedType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        final List<TypeReference> oldTypeArguments = ((IGenericInstance)t).getTypeArguments();
        TypeReference[] newTypeArguments = null;
        boolean changed = false;
        for (int i = 0; i < oldTypeArguments.size(); ++i) {
            final TypeReference oldTypeArgument = oldTypeArguments.get(i);
            final TypeReference newTypeArgument = this.visit(oldTypeArgument, map);
            if (newTypeArguments != null) {
                newTypeArguments[i] = newTypeArgument;
            }
            else if (oldTypeArgument != newTypeArgument) {
                newTypeArguments = new TypeReference[oldTypeArguments.size()];
                oldTypeArguments.toArray(newTypeArguments);
                newTypeArguments[i] = newTypeArgument;
                changed = true;
            }
        }
        if (changed) {
            return t.makeGenericType(newTypeArguments);
        }
        return t;
    }
    
    @Override
    public TypeReference visitPrimitiveType(final PrimitiveType t, final Map<TypeReference, TypeReference> map) {
        return t;
    }
    
    @Override
    public TypeReference visitClassType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        final TypeReference resolvedType = t.isGenericType() ? t : t.resolve();
        if (resolvedType == null || !resolvedType.isGenericDefinition()) {
            return t;
        }
        final List<TypeReference> oldTypeArguments = (List<GenericParameter>)resolvedType.getGenericParameters();
        TypeReference[] newTypeArguments = null;
        boolean changed = false;
        for (int i = 0; i < oldTypeArguments.size(); ++i) {
            final TypeReference oldTypeArgument = oldTypeArguments.get(i);
            final TypeReference newTypeArgument = this.visit(oldTypeArgument, map);
            if (newTypeArguments != null) {
                newTypeArguments[i] = newTypeArgument;
            }
            else if (oldTypeArgument != newTypeArgument) {
                newTypeArguments = new TypeReference[oldTypeArguments.size()];
                oldTypeArguments.toArray(newTypeArguments);
                newTypeArguments[i] = newTypeArgument;
                changed = true;
            }
        }
        if (changed) {
            return t.makeGenericType(newTypeArguments);
        }
        return t;
    }
    
    @Override
    public TypeReference visitNullType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        return t;
    }
    
    @Override
    public TypeReference visitBottomType(final TypeReference t, final Map<TypeReference, TypeReference> map) {
        return t;
    }
    
    @Override
    public TypeReference visitRawType(final RawType t, final Map<TypeReference, TypeReference> map) {
        return t;
    }
    
    @Override
    public MethodReference visitParameterizedMethod(final MethodReference m, final Map<TypeReference, TypeReference> map) {
        return this.visitMethod(m, map);
    }
    
    @Override
    public MethodReference visitMethod(final MethodReference m, final Map<TypeReference, TypeReference> map) {
        final MethodDefinition resolvedMethod = m.resolve();
        List<TypeReference> oldTypeArguments;
        if (m instanceof IGenericInstance) {
            oldTypeArguments = ((IGenericInstance)m).getTypeArguments();
        }
        else if (m.isGenericDefinition()) {
            oldTypeArguments = (List<GenericParameter>)m.getGenericParameters();
        }
        else {
            oldTypeArguments = Collections.emptyList();
        }
        final List<TypeReference> newTypeArguments = this.visitTypes(oldTypeArguments, map);
        final TypeReference oldReturnType = m.getReturnType();
        final TypeReference newReturnType = this.visit(oldReturnType, map);
        final List<ParameterDefinition> oldParameters = m.getParameters();
        final List<ParameterDefinition> newParameters = this.visitParameters(oldParameters, map);
        if (newTypeArguments != oldTypeArguments || newReturnType != oldReturnType || newParameters != oldParameters) {
            return new GenericMethodInstance(this.visit(m.getDeclaringType(), map), (resolvedMethod != null) ? resolvedMethod : m, newReturnType, (newParameters == oldParameters) ? MetadataHelper.copyParameters(oldParameters) : newParameters, newTypeArguments);
        }
        return m;
    }
    
    protected List<TypeReference> visitTypes(final List<TypeReference> types, final Map<TypeReference, TypeReference> map) {
        TypeReference[] newTypes = null;
        boolean changed = false;
        for (int i = 0; i < types.size(); ++i) {
            final TypeReference oldTypeArgument = types.get(i);
            final TypeReference newTypeArgument = this.visit(oldTypeArgument, map);
            if (newTypes != null) {
                newTypes[i] = newTypeArgument;
            }
            else if (oldTypeArgument != newTypeArgument) {
                newTypes = new TypeReference[types.size()];
                types.toArray(newTypes);
                newTypes[i] = newTypeArgument;
                changed = true;
            }
        }
        return changed ? ArrayUtilities.asUnmodifiableList(newTypes) : types;
    }
    
    protected List<ParameterDefinition> visitParameters(final List<ParameterDefinition> parameters, final Map<TypeReference, TypeReference> map) {
        if (parameters.isEmpty()) {
            return parameters;
        }
        ParameterDefinition[] newParameters = null;
        boolean changed = false;
        for (int i = 0; i < parameters.size(); ++i) {
            final ParameterDefinition oldParameter = parameters.get(i);
            final TypeReference oldType = oldParameter.getParameterType();
            final TypeReference newType = this.visit(oldType, map);
            final ParameterDefinition newParameter = (oldType != newType) ? new ParameterDefinition(oldParameter.getSlot(), newType) : oldParameter;
            if (newParameters != null) {
                newParameters[i] = newParameter;
            }
            else if (oldType != newType) {
                newParameters = new ParameterDefinition[parameters.size()];
                parameters.toArray(newParameters);
                newParameters[i] = newParameter;
                changed = true;
            }
        }
        return changed ? ArrayUtilities.asUnmodifiableList(newParameters) : parameters;
    }
    
    @Override
    public FieldReference visitField(final FieldReference f, final Map<TypeReference, TypeReference> map) {
        final TypeReference oldFieldType = f.getFieldType();
        final TypeReference newFieldType = this.visit(oldFieldType, map);
        if (newFieldType != oldFieldType) {
            final TypeReference declaringType = f.getDeclaringType();
            return new FieldReference(f, newFieldType) {
                private final String _name = param_1.getName();
                private final TypeReference _type = param_2;
                
                @Override
                public TypeReference getFieldType() {
                    return this._type;
                }
                
                @Override
                public TypeReference getDeclaringType() {
                    return declaringType;
                }
                
                @Override
                public String getName() {
                    return this._name;
                }
                
                @Override
                protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
                    if (fullName) {
                        final TypeReference declaringType = this.getDeclaringType();
                        if (declaringType != null) {
                            return declaringType.appendName(sb, true, false).append('.').append(this.getName());
                        }
                    }
                    return sb.append(this._name);
                }
            };
        }
        return f;
    }
    
    static /* synthetic */ int[] $SWITCH_TABLE$com$strobel$assembler$metadata$JvmType() {
        final int[] loc_0 = TypeSubstitutionVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType;
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
        return TypeSubstitutionVisitor.$SWITCH_TABLE$com$strobel$assembler$metadata$JvmType = loc_1;
    }
}
