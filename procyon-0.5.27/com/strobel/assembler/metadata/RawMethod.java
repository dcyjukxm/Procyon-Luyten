package com.strobel.assembler.metadata;

import com.strobel.core.*;
import java.util.*;

public final class RawMethod extends MethodReference implements IGenericInstance
{
    private final MethodReference _baseMethod;
    private final TypeReference _returnType;
    private final ParameterDefinitionCollection _parameters;
    private TypeReference _declaringType;
    
    public RawMethod(final MethodReference baseMethod) {
        super();
        VerifyArgument.notNull(baseMethod, "baseMethod");
        final TypeReference declaringType = baseMethod.getDeclaringType();
        this._baseMethod = baseMethod;
        this._declaringType = MetadataHelper.eraseRecursive(declaringType);
        this._returnType = MetadataHelper.eraseRecursive(baseMethod.getReturnType());
        this._parameters = new ParameterDefinitionCollection(this);
        for (final ParameterDefinition parameter : baseMethod.getParameters()) {
            if (parameter.hasName()) {
                this._parameters.add(new ParameterDefinition(parameter.getSlot(), parameter.getName(), MetadataHelper.eraseRecursive(parameter.getParameterType())));
            }
            else {
                this._parameters.add(new ParameterDefinition(parameter.getSlot(), MetadataHelper.eraseRecursive(parameter.getParameterType())));
            }
        }
        this._parameters.freeze();
    }
    
    public final MethodReference getBaseMethod() {
        return this._baseMethod;
    }
    
    @Override
    public final boolean hasTypeArguments() {
        return false;
    }
    
    @Override
    public final List<TypeReference> getTypeArguments() {
        return Collections.emptyList();
    }
    
    @Override
    public final IGenericParameterProvider getGenericDefinition() {
        return (this._baseMethod instanceof IGenericInstance) ? ((IGenericInstance)this._baseMethod).getGenericDefinition() : null;
    }
    
    @Override
    public final List<GenericParameter> getGenericParameters() {
        return Collections.emptyList();
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
        return this._baseMethod.resolve();
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
        return this._baseMethod.getName();
    }
}
