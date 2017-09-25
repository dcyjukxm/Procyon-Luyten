package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;

final class GenericMethodInstance extends MethodReference implements IGenericInstance
{
    private final MethodReference _genericDefinition;
    private final TypeReference _returnType;
    private final ParameterDefinitionCollection _parameters;
    private final List<TypeReference> _typeArguments;
    private TypeReference _declaringType;
    
    GenericMethodInstance(final TypeReference declaringType, final MethodReference definition, final TypeReference returnType, final List<ParameterDefinition> parameters, final List<TypeReference> typeArguments) {
        super();
        this._declaringType = VerifyArgument.notNull(declaringType, "declaringType");
        this._genericDefinition = VerifyArgument.notNull(definition, "definition");
        this._returnType = VerifyArgument.notNull(returnType, "returnType");
        this._parameters = new ParameterDefinitionCollection(this);
        this._typeArguments = VerifyArgument.notNull(typeArguments, "typeArguments");
        this._parameters.addAll(VerifyArgument.notNull(parameters, "parameters"));
        this._parameters.freeze();
    }
    
    @Override
    public final boolean hasTypeArguments() {
        return !this._typeArguments.isEmpty();
    }
    
    @Override
    public final List<TypeReference> getTypeArguments() {
        return this._typeArguments;
    }
    
    @Override
    public final IGenericParameterProvider getGenericDefinition() {
        return this._genericDefinition;
    }
    
    @Override
    public final List<GenericParameter> getGenericParameters() {
        return this._genericDefinition.getGenericParameters();
    }
    
    @Override
    public final TypeReference getReturnType() {
        return this._returnType;
    }
    
    @Override
    public final List<ParameterDefinition> getParameters() {
        return this._parameters;
    }
    
    @Override
    public boolean isGenericMethod() {
        return this.hasTypeArguments();
    }
    
    @Override
    public MethodDefinition resolve() {
        return this._genericDefinition.resolve();
    }
    
    @Override
    public StringBuilder appendErasedSignature(final StringBuilder sb) {
        return this._genericDefinition.appendErasedSignature(sb);
    }
    
    @Override
    public final TypeReference getDeclaringType() {
        return this._declaringType;
    }
    
    final void setDeclaringType(final TypeReference declaringType) {
        this._declaringType = declaringType;
    }
    
    @Override
    public final String getName() {
        return this._genericDefinition.getName();
    }
}
