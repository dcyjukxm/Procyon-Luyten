package com.strobel.assembler.metadata;

import java.util.*;

final class ParameterizedType extends TypeReference implements IGenericInstance
{
    private final TypeReference _genericDefinition;
    private final List<TypeReference> _typeParameters;
    
    ParameterizedType(final TypeReference genericDefinition, final List<TypeReference> typeParameters) {
        super();
        this._genericDefinition = genericDefinition;
        this._typeParameters = typeParameters;
    }
    
    @Override
    public String getName() {
        return this._genericDefinition.getName();
    }
    
    @Override
    public String getPackageName() {
        return this._genericDefinition.getPackageName();
    }
    
    @Override
    public String getFullName() {
        return this._genericDefinition.getFullName();
    }
    
    @Override
    public String getInternalName() {
        return this._genericDefinition.getInternalName();
    }
    
    @Override
    public TypeReference getDeclaringType() {
        return this._genericDefinition.getDeclaringType();
    }
    
    @Override
    public String getSimpleName() {
        return this._genericDefinition.getSimpleName();
    }
    
    @Override
    public boolean isGenericDefinition() {
        return false;
    }
    
    @Override
    public List<GenericParameter> getGenericParameters() {
        if (!this._genericDefinition.isGenericDefinition()) {
            final TypeDefinition resolvedDefinition = this._genericDefinition.resolve();
            if (resolvedDefinition != null) {
                return resolvedDefinition.getGenericParameters();
            }
        }
        return this._genericDefinition.getGenericParameters();
    }
    
    @Override
    public boolean hasTypeArguments() {
        return true;
    }
    
    @Override
    public List<TypeReference> getTypeArguments() {
        return this._typeParameters;
    }
    
    @Override
    public IGenericParameterProvider getGenericDefinition() {
        return this._genericDefinition;
    }
    
    @Override
    public TypeReference getUnderlyingType() {
        return this._genericDefinition;
    }
    
    @Override
    public final <R, P> R accept(final TypeMetadataVisitor<P, R> visitor, final P parameter) {
        return visitor.visitParameterizedType(this, parameter);
    }
    
    @Override
    public TypeDefinition resolve() {
        return this._genericDefinition.resolve();
    }
    
    @Override
    public FieldDefinition resolve(final FieldReference field) {
        return this._genericDefinition.resolve(field);
    }
    
    @Override
    public MethodDefinition resolve(final MethodReference method) {
        return this._genericDefinition.resolve(method);
    }
    
    @Override
    public TypeDefinition resolve(final TypeReference type) {
        return this._genericDefinition.resolve(type);
    }
}
