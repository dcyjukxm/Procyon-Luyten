package com.strobel.assembler.metadata;

import com.strobel.assembler.*;
import com.strobel.core.*;

public final class AnonymousLocalTypeCollection extends Collection<TypeDefinition>
{
    private final MethodDefinition _owner;
    
    public AnonymousLocalTypeCollection(final MethodDefinition owner) {
        super();
        this._owner = VerifyArgument.notNull(owner, "owner");
    }
    
    @Override
    protected void afterAdd(final int index, final TypeDefinition type, final boolean appended) {
        type.setDeclaringMethod(this._owner);
    }
    
    @Override
    protected void beforeSet(final int index, final TypeDefinition type) {
        final TypeDefinition current = this.get(index);
        current.setDeclaringMethod(null);
        type.setDeclaringMethod(this._owner);
    }
    
    @Override
    protected void afterRemove(final int index, final TypeDefinition type) {
        type.setDeclaringMethod(null);
    }
    
    @Override
    protected void beforeClear() {
        for (int i = 0; i < this.size(); ++i) {
            this.get(i).setDeclaringMethod(null);
        }
    }
}
