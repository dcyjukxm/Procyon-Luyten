package com.strobel.assembler.metadata;

import com.strobel.core.*;

public abstract class VariableReference extends Freezable implements IMetadataTypeMember
{
    private String _name;
    private TypeReference _variableType;
    
    protected VariableReference(final TypeReference variableType) {
        super();
        this._variableType = variableType;
    }
    
    protected VariableReference(final String name, final TypeReference variableType) {
        super();
        this._name = name;
        this._variableType = variableType;
    }
    
    @Override
    public final String getName() {
        return this._name;
    }
    
    @Override
    public abstract TypeReference getDeclaringType();
    
    public final boolean hasName() {
        return !StringUtilities.isNullOrEmpty(this._name);
    }
    
    protected final void setName(final String name) {
        this._name = name;
    }
    
    public final TypeReference getVariableType() {
        return this._variableType;
    }
    
    protected final void setVariableType(final TypeReference variableType) {
        this._variableType = variableType;
    }
    
    public abstract int getSlot();
    
    public abstract VariableDefinition resolve();
    
    @Override
    public String toString() {
        return this.getName();
    }
}
