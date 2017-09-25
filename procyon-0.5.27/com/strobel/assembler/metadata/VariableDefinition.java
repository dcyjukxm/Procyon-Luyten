package com.strobel.assembler.metadata;

import com.strobel.core.*;

public final class VariableDefinition extends VariableReference
{
    private final int _slot;
    private final MethodDefinition _declaringMethod;
    private int _scopeStart;
    private int _scopeEnd;
    private boolean _isTypeKnown;
    private boolean _fromMetadata;
    private ParameterDefinition _parameter;
    
    public VariableDefinition(final int slot, final String name, final MethodDefinition declaringMethod) {
        super(name, VerifyArgument.notNull(declaringMethod, "declaringMethod").getDeclaringType());
        this._declaringMethod = declaringMethod;
        this._slot = slot;
    }
    
    public VariableDefinition(final int slot, final String name, final MethodDefinition declaringMethod, final TypeReference variableType) {
        this(slot, name, declaringMethod);
        this.setVariableType(variableType);
    }
    
    public final boolean isParameter() {
        return this._parameter != null;
    }
    
    public final ParameterDefinition getParameter() {
        return this._parameter;
    }
    
    public final void setParameter(final ParameterDefinition parameter) {
        this.verifyNotFrozen();
        this._parameter = parameter;
    }
    
    @Override
    public final TypeReference getDeclaringType() {
        return this._declaringMethod.getDeclaringType();
    }
    
    @Override
    public final int getSlot() {
        return this._slot;
    }
    
    public final int getSize() {
        return this.getVariableType().getSimpleType().stackSlots();
    }
    
    public final int getScopeStart() {
        return this._scopeStart;
    }
    
    public final void setScopeStart(final int scopeStart) {
        this.verifyNotFrozen();
        this._scopeStart = scopeStart;
    }
    
    public final int getScopeEnd() {
        return this._scopeEnd;
    }
    
    public final void setScopeEnd(final int scopeEnd) {
        this.verifyNotFrozen();
        this._scopeEnd = scopeEnd;
    }
    
    public final boolean isTypeKnown() {
        return this._isTypeKnown;
    }
    
    public final void setTypeKnown(final boolean typeKnown) {
        this.verifyNotFrozen();
        this._isTypeKnown = typeKnown;
    }
    
    public final boolean isFromMetadata() {
        return this._fromMetadata;
    }
    
    public final void setFromMetadata(final boolean fromMetadata) {
        this.verifyNotFrozen();
        this._fromMetadata = fromMetadata;
    }
    
    @Override
    public VariableDefinition resolve() {
        return this;
    }
    
    @Override
    public String toString() {
        return "VariableDefinition{Slot=" + this._slot + ", ScopeStart=" + this._scopeStart + ", ScopeEnd=" + this._scopeEnd + ", Name=" + this.getName() + ", IsFromMetadata=" + this._fromMetadata + ", IsTypeKnown=" + this._isTypeKnown + ", Type=" + this.getVariableType().getSignature() + '}';
    }
}
