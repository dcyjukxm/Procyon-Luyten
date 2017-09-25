package com.strobel.assembler.metadata;

import com.strobel.core.*;

public abstract class ParameterReference implements IMetadataTypeMember
{
    private String _name;
    private int _position;
    private TypeReference _parameterType;
    
    protected ParameterReference(final String name, final TypeReference parameterType) {
        super();
        this._position = -1;
        this._name = ((name != null) ? name : "");
        this._parameterType = VerifyArgument.notNull(parameterType, "parameterType");
    }
    
    @Override
    public abstract TypeReference getDeclaringType();
    
    @Override
    public String getName() {
        if (!StringUtilities.isNullOrEmpty(this._name)) {
            return this._name;
        }
        if (this._position < 0) {
            return this._name;
        }
        return "param_" + this._position;
    }
    
    public final boolean hasName() {
        return !StringUtilities.isNullOrEmpty(this._name);
    }
    
    protected void setName(final String name) {
        this._name = name;
    }
    
    public int getPosition() {
        return this._position;
    }
    
    protected void setPosition(final int position) {
        this._position = position;
    }
    
    public TypeReference getParameterType() {
        return this._parameterType;
    }
    
    protected void setParameterType(final TypeReference parameterType) {
        this._parameterType = parameterType;
    }
    
    @Override
    public String toString() {
        return this.getName();
    }
    
    public abstract ParameterDefinition resolve();
}
