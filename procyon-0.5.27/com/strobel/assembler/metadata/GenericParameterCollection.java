package com.strobel.assembler.metadata;

import com.strobel.assembler.*;
import com.strobel.core.*;

public final class GenericParameterCollection extends Collection<GenericParameter>
{
    private final IGenericParameterProvider _owner;
    
    public GenericParameterCollection(final IGenericParameterProvider owner) {
        super();
        this._owner = VerifyArgument.notNull(owner, "owner");
    }
    
    private void updateGenericParameter(final int index, final GenericParameter p) {
        p.setOwner(this._owner);
        p.setPosition(index);
    }
    
    @Override
    protected void afterAdd(final int index, final GenericParameter p, final boolean appended) {
        this.updateGenericParameter(index, p);
        if (!appended) {
            for (int i = index + 1; i < this.size(); ++i) {
                this.get(i).setPosition(i + 1);
            }
        }
    }
    
    @Override
    protected void beforeSet(final int index, final GenericParameter p) {
        final GenericParameter current = this.get(index);
        current.setOwner(null);
        current.setPosition(-1);
        this.updateGenericParameter(index, p);
    }
    
    @Override
    protected void afterRemove(final int index, final GenericParameter p) {
        p.setOwner(null);
        p.setPosition(-1);
        for (int i = index; i < this.size(); ++i) {
            this.get(i).setPosition(i);
        }
    }
    
    @Override
    protected void beforeClear() {
        super.beforeClear();
    }
}
