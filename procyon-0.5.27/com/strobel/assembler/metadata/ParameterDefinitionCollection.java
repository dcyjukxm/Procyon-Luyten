package com.strobel.assembler.metadata;

import com.strobel.assembler.*;

public final class ParameterDefinitionCollection extends Collection<ParameterDefinition>
{
    final IMethodSignature signature;
    private TypeReference _declaringType;
    
    ParameterDefinitionCollection(final IMethodSignature signature) {
        super();
        this.signature = signature;
    }
    
    public final TypeReference getDeclaringType() {
        return this._declaringType;
    }
    
    final void setDeclaringType(final TypeReference declaringType) {
        this._declaringType = declaringType;
        for (int i = 0; i < this.size(); ++i) {
            this.get(i).setDeclaringType(declaringType);
        }
    }
    
    @Override
    protected void afterAdd(final int index, final ParameterDefinition p, final boolean appended) {
        p.setMethod(this.signature);
        p.setPosition(index);
        p.setDeclaringType(this._declaringType);
        if (!appended) {
            for (int i = index + 1; i < this.size(); ++i) {
                this.get(i).setPosition(i + 1);
            }
        }
    }
    
    @Override
    protected void beforeSet(final int index, final ParameterDefinition p) {
        final ParameterDefinition current = this.get(index);
        current.setMethod(null);
        current.setPosition(-1);
        current.setDeclaringType(null);
        p.setMethod(this.signature);
        p.setPosition(index);
        p.setDeclaringType(this._declaringType);
    }
    
    @Override
    protected void afterRemove(final int index, final ParameterDefinition p) {
        p.setMethod(null);
        p.setPosition(-1);
        p.setDeclaringType(null);
        for (int i = index; i < this.size(); ++i) {
            this.get(i).setPosition(i);
        }
    }
    
    @Override
    protected void beforeClear() {
        for (int i = 0; i < this.size(); ++i) {
            this.get(i).setMethod(null);
            this.get(i).setPosition(-1);
            this.get(i).setDeclaringType(null);
        }
    }
}
