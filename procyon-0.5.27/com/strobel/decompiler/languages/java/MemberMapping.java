package com.strobel.decompiler.languages.java;

import com.strobel.decompiler.ast.*;
import com.strobel.assembler.metadata.*;

public final class MemberMapping
{
    private MemberReference _memberReference;
    private Iterable<Variable> _localVariables;
    
    MemberMapping() {
        super();
    }
    
    public MemberMapping(final MethodDefinition method) {
        super();
        this.setMemberReference(method);
    }
    
    public MemberReference getMemberReference() {
        return this._memberReference;
    }
    
    public void setMemberReference(final MemberReference memberReference) {
        this._memberReference = memberReference;
    }
    
    public Iterable<Variable> getLocalVariables() {
        return this._localVariables;
    }
    
    public void setLocalVariables(final Iterable<Variable> localVariables) {
        this._localVariables = localVariables;
    }
}
