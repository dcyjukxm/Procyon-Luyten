package com.strobel.assembler.metadata;

import com.strobel.util.*;
import com.strobel.core.*;
import java.util.*;

public abstract class TypeReference extends MemberReference implements IGenericParameterProvider, IGenericContext
{
    private String _name;
    private TypeReference _declaringType;
    private ArrayType _arrayType;
    
    @Override
    public boolean containsGenericParameters() {
        if (this.isGenericType()) {
            if (this.isGenericDefinition()) {
                if (this.hasGenericParameters()) {
                    return true;
                }
            }
            else if (this instanceof IGenericInstance) {
                final List<TypeReference> typeArguments = ((IGenericInstance)this).getTypeArguments();
                for (int i = 0, n = typeArguments.size(); i < n; ++i) {
                    if (typeArguments.get(i).containsGenericParameters()) {
                        return true;
                    }
                }
            }
        }
        return super.containsGenericParameters();
    }
    
    @Override
    public String getName() {
        return this._name;
    }
    
    public String getPackageName() {
        return "";
    }
    
    @Override
    public TypeReference getDeclaringType() {
        return this._declaringType;
    }
    
    @Override
    public boolean isEquivalentTo(final MemberReference member) {
        return member instanceof TypeReference && MetadataResolver.areEquivalent(this, (TypeReference)member);
    }
    
    protected void setName(final String name) {
        this._name = name;
    }
    
    protected final void setDeclaringType(final TypeReference declaringType) {
        this._declaringType = declaringType;
    }
    
    public abstract String getSimpleName();
    
    @Override
    public String getFullName() {
        final StringBuilder name = new StringBuilder();
        this.appendName(name, true, true);
        return name.toString();
    }
    
    public String getInternalName() {
        final StringBuilder name = new StringBuilder();
        this.appendName(name, true, false);
        return name.toString();
    }
    
    public TypeReference getUnderlyingType() {
        return this;
    }
    
    public TypeReference getElementType() {
        return null;
    }
    
    public abstract <R, P> R accept(final TypeMetadataVisitor<P, R> param_0, final P param_1);
    
    @Override
    public int hashCode() {
        return this.getInternalName().hashCode();
    }
    
    @Override
    public boolean equals(final Object obj) {
        return obj instanceof TypeReference && MetadataHelper.isSameType(this, (TypeReference)obj, true);
    }
    
    public TypeReference makeArrayType() {
        if (this._arrayType == null) {
            synchronized (this) {
                if (this._arrayType == null) {
                    this._arrayType = ArrayType.create(this);
                }
            }
        }
        return this._arrayType;
    }
    
    public TypeReference makeGenericType(final List<? extends TypeReference> typeArguments) {
        VerifyArgument.notNull(typeArguments, "typeArguments");
        return this.makeGenericType((TypeReference[])typeArguments.toArray(new TypeReference[typeArguments.size()]));
    }
    
    public TypeReference makeGenericType(final TypeReference... typeArguments) {
        VerifyArgument.noNullElementsAndNotEmpty(typeArguments, "typeArguments");
        if (this.isGenericDefinition()) {
            return new ParameterizedType(this, ArrayUtilities.asUnmodifiableList(typeArguments));
        }
        if (this instanceof IGenericInstance) {
            return new ParameterizedType((TypeReference)((IGenericInstance)this).getGenericDefinition(), ArrayUtilities.asUnmodifiableList(typeArguments));
        }
        throw Error.notGenericType(this);
    }
    
    public boolean isWildcardType() {
        return false;
    }
    
    public boolean isCompoundType() {
        return false;
    }
    
    public boolean isBoundedType() {
        return this.isGenericParameter() || this.isWildcardType() || this instanceof ICapturedType || this instanceof CompoundTypeReference;
    }
    
    public boolean isUnbounded() {
        return true;
    }
    
    public boolean hasExtendsBound() {
        return this.isGenericParameter() || (this.isWildcardType() && !BuiltinTypes.Object.equals(this.getExtendsBound()) && MetadataResolver.areEquivalent(BuiltinTypes.Bottom, this.getSuperBound()));
    }
    
    public boolean hasSuperBound() {
        return this.isWildcardType() && !MetadataResolver.areEquivalent(BuiltinTypes.Bottom, this.getSuperBound());
    }
    
    public TypeReference getExtendsBound() {
        throw ContractUtils.unsupported();
    }
    
    public TypeReference getSuperBound() {
        throw ContractUtils.unsupported();
    }
    
    public JvmType getSimpleType() {
        return JvmType.Object;
    }
    
    public boolean isNested() {
        return this.getDeclaringType() != null && !this.isGenericParameter();
    }
    
    public boolean isArray() {
        return this.getSimpleType() == JvmType.Array;
    }
    
    public boolean isPrimitive() {
        return false;
    }
    
    public boolean isVoid() {
        return false;
    }
    
    @Override
    public boolean hasGenericParameters() {
        return !this.getGenericParameters().isEmpty();
    }
    
    @Override
    public boolean isGenericDefinition() {
        return this.hasGenericParameters() && this.isDefinition();
    }
    
    @Override
    public List<GenericParameter> getGenericParameters() {
        return Collections.emptyList();
    }
    
    public boolean isGenericParameter() {
        return this.getSimpleType() == JvmType.TypeVariable;
    }
    
    public boolean isGenericType() {
        return this.hasGenericParameters();
    }
    
    public TypeReference getRawType() {
        if (!this.isGenericType()) {
            throw ContractUtils.unsupported();
        }
        final TypeReference underlyingType = this.getUnderlyingType();
        if (underlyingType != this) {
            return underlyingType.getRawType();
        }
        return new RawType(this);
    }
    
    @Override
    public GenericParameter findTypeVariable(final String name) {
        for (final GenericParameter genericParameter : this.getGenericParameters()) {
            if (StringUtilities.equals(genericParameter.getName(), name)) {
                return genericParameter;
            }
        }
        final TypeReference declaringType = this.getDeclaringType();
        if (declaringType != null) {
            return declaringType.findTypeVariable(name);
        }
        return null;
    }
    
    public String getBriefDescription() {
        return this.appendBriefDescription(new StringBuilder()).toString();
    }
    
    public String getDescription() {
        return this.appendDescription(new StringBuilder()).toString();
    }
    
    public String getErasedDescription() {
        return this.appendErasedDescription(new StringBuilder()).toString();
    }
    
    public String getSimpleDescription() {
        return this.appendSimpleDescription(new StringBuilder()).toString();
    }
    
    @Override
    protected StringBuilder appendName(final StringBuilder sb, final boolean fullName, final boolean dottedName) {
        final String simpleName = this.getSimpleName();
        final TypeReference declaringType = this.getDeclaringType();
        if (dottedName && simpleName != null && declaringType != null) {
            return declaringType.appendName(sb, fullName, true).append('.').append(simpleName);
        }
        final String name = fullName ? this.getName() : simpleName;
        final String packageName = fullName ? this.getPackageName() : null;
        if (StringUtilities.isNullOrEmpty(packageName)) {
            return sb.append(name);
        }
        if (dottedName) {
            return sb.append(packageName).append('.').append(name);
        }
        for (int packageEnd = packageName.length(), i = 0; i < packageEnd; ++i) {
            final char c = packageName.charAt(i);
            sb.append((c == '.') ? '/' : c);
        }
        sb.append('/');
        return sb.append(name);
    }
    
    protected StringBuilder appendBriefDescription(final StringBuilder sb) {
        StringBuilder s = this.appendName(sb, true, true);
        List<? extends TypeReference> typeArguments;
        if (this instanceof IGenericInstance) {
            typeArguments = ((IGenericInstance)this).getTypeArguments();
        }
        else if (this.isGenericDefinition()) {
            typeArguments = this.getGenericParameters();
        }
        else {
            typeArguments = Collections.emptyList();
        }
        final int count = typeArguments.size();
        if (count > 0) {
            s.append('<');
            for (int i = 0; i < count; ++i) {
                if (i != 0) {
                    s.append(", ");
                }
                s = ((TypeReference)typeArguments.get(i)).appendBriefDescription(s);
            }
            s.append('>');
        }
        return s;
    }
    
    protected StringBuilder appendSimpleDescription(final StringBuilder sb) {
        StringBuilder s = sb.append(this.getSimpleName());
        if (this.isGenericType()) {
            List<? extends TypeReference> typeArguments;
            if (this instanceof IGenericInstance) {
                typeArguments = ((IGenericInstance)this).getTypeArguments();
            }
            else {
                typeArguments = this.getGenericParameters();
            }
            final int count = typeArguments.size();
            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    if (i != 0) {
                        s.append(", ");
                    }
                    final TypeReference typeArgument = (TypeReference)typeArguments.get(i);
                    if (typeArgument instanceof GenericParameter) {
                        s.append(typeArgument.getSimpleName());
                    }
                    else {
                        s = typeArgument.appendSimpleDescription(s);
                    }
                }
                s.append('>');
            }
        }
        return s;
    }
    
    protected StringBuilder appendErasedDescription(final StringBuilder sb) {
        return this.appendName(sb, true, true);
    }
    
    protected StringBuilder appendDescription(final StringBuilder sb) {
        StringBuilder s = this.appendName(sb, false, true);
        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance)this).getTypeArguments();
            final int count = typeArguments.size();
            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    if (i != 0) {
                        s.append(", ");
                    }
                    s = typeArguments.get(i).appendBriefDescription(s);
                }
                s.append('>');
            }
        }
        return s;
    }
    
    @Override
    protected StringBuilder appendSignature(final StringBuilder sb) {
        if (this.isGenericParameter()) {
            sb.append('T');
            sb.append(this.getName());
            sb.append(';');
            return sb;
        }
        return this.appendClassSignature(sb);
    }
    
    @Override
    protected StringBuilder appendErasedSignature(final StringBuilder sb) {
        if (this.isGenericType() && !this.isGenericDefinition()) {
            return this.getUnderlyingType().appendErasedSignature(sb);
        }
        return this.appendErasedClassSignature(sb);
    }
    
    @Override
    public String toString() {
        return this.getBriefDescription();
    }
    
    protected StringBuilder appendGenericSignature(final StringBuilder sb) {
        StringBuilder s = sb;
        if (this.isGenericParameter()) {
            final TypeReference extendsBound = this.getExtendsBound();
            final TypeDefinition resolvedBound = extendsBound.resolve();
            s.append(this.getName());
            if (resolvedBound != null && resolvedBound.isInterface()) {
                s.append(':');
            }
            s.append(':');
            s = extendsBound.appendSignature(s);
            return s;
        }
        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance)this).getTypeArguments();
            final int count = typeArguments.size();
            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    s = typeArguments.get(i).appendGenericSignature(s);
                }
                s.append('>');
            }
        }
        return s;
    }
    
    protected StringBuilder appendClassSignature(final StringBuilder sb) {
        sb.append('L');
        StringBuilder s = this.appendName(sb, true, false);
        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance)this).getTypeArguments();
            final int count = typeArguments.size();
            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    final TypeReference type = typeArguments.get(i);
                    if (type.isGenericDefinition()) {
                        s = type.appendErasedSignature(s);
                    }
                    else {
                        s = type.appendSignature(s);
                    }
                }
                s.append('>');
            }
        }
        s.append(';');
        return s;
    }
    
    protected StringBuilder appendErasedClassSignature(StringBuilder sb) {
        sb.append('L');
        sb = this.appendName(sb, true, false);
        sb.append(';');
        return sb;
    }
    
    protected StringBuilder appendClassDescription(final StringBuilder sb) {
        StringBuilder s = sb;
        this.appendName(sb, true, true);
        if (this instanceof IGenericInstance) {
            final List<TypeReference> typeArguments = ((IGenericInstance)this).getTypeArguments();
            final int count = typeArguments.size();
            if (count > 0) {
                s.append('<');
                for (int i = 0; i < count; ++i) {
                    s = typeArguments.get(i).appendErasedClassSignature(s);
                }
                s.append('>');
            }
        }
        return s;
    }
    
    public TypeDefinition resolve() {
        final TypeReference declaringType = this.getDeclaringType();
        return (declaringType != null) ? declaringType.resolve(this) : null;
    }
    
    public FieldDefinition resolve(final FieldReference field) {
        final TypeDefinition resolvedType = this.resolve();
        if (resolvedType != null) {
            return MetadataResolver.getField(resolvedType.getDeclaredFields(), field);
        }
        return null;
    }
    
    public MethodDefinition resolve(final MethodReference method) {
        final TypeDefinition resolvedType = this.resolve();
        if (resolvedType != null) {
            return MetadataResolver.getMethod(resolvedType.getDeclaredMethods(), method);
        }
        return null;
    }
    
    public TypeDefinition resolve(final TypeReference type) {
        final TypeDefinition resolvedType = this.resolve();
        if (resolvedType != null) {
            return MetadataResolver.getNestedType(resolvedType.getDeclaredTypes(), type);
        }
        return null;
    }
}
