package com.strobel.decompiler.ast;

import com.strobel.assembler.metadata.*;

public final class Variable
{
    private String _name;
    private boolean _isGenerated;
    private boolean _isLambdaParameter;
    private TypeReference _type;
    private VariableDefinition _originalVariable;
    private ParameterDefinition _originalParameter;
    
    public final String getName() {
        return this._name;
    }
    
    public final void setName(final String name) {
        this._name = name;
    }
    
    public final boolean isParameter() {
        if (this._originalParameter != null) {
            return true;
        }
        final VariableDefinition originalVariable = this._originalVariable;
        return originalVariable != null && originalVariable.isParameter();
    }
    
    public final boolean isGenerated() {
        return this._isGenerated;
    }
    
    public final void setGenerated(final boolean generated) {
        this._isGenerated = generated;
    }
    
    public final TypeReference getType() {
        return this._type;
    }
    
    public final void setType(final TypeReference type) {
        this._type = type;
    }
    
    public final VariableDefinition getOriginalVariable() {
        return this._originalVariable;
    }
    
    public final void setOriginalVariable(final VariableDefinition originalVariable) {
        this._originalVariable = originalVariable;
    }
    
    public final ParameterDefinition getOriginalParameter() {
        final ParameterDefinition originalParameter = this._originalParameter;
        if (originalParameter != null) {
            return originalParameter;
        }
        final VariableDefinition originalVariable = this._originalVariable;
        if (originalVariable != null) {
            return originalVariable.getParameter();
        }
        return null;
    }
    
    public final void setOriginalParameter(final ParameterDefinition originalParameter) {
        this._originalParameter = originalParameter;
    }
    
    public final boolean isLambdaParameter() {
        return this._isLambdaParameter;
    }
    
    public final void setLambdaParameter(final boolean lambdaParameter) {
        this._isLambdaParameter = lambdaParameter;
    }
    
    @Override
    public final String toString() {
        return this._name;
    }
}
