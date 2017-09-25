package com.strobel.core;

public abstract class Freezable implements IFreezable
{
    private boolean _isFrozen;
    
    @Override
    public boolean canFreeze() {
        return !this.isFrozen();
    }
    
    @Override
    public final boolean isFrozen() {
        return this._isFrozen;
    }
    
    @Override
    public final void freeze() throws IllegalStateException {
        if (!this.canFreeze()) {
            throw new IllegalStateException("Object cannot be frozen.  Be sure to check canFreeze() before calling freeze(), or use the tryFreeze() method instead.");
        }
        this.freezeCore();
        this._isFrozen = true;
    }
    
    protected void freezeCore() {
    }
    
    protected final void verifyNotFrozen() {
        if (this.isFrozen()) {
            throw new IllegalStateException("Frozen object cannot be modified.");
        }
    }
    
    protected final void verifyFrozen() {
        if (!this.isFrozen()) {
            throw new IllegalStateException("Object must be frozen before performing this operation.");
        }
    }
    
    @Override
    public final boolean tryFreeze() {
        if (!this.canFreeze()) {
            return false;
        }
        try {
            this.freeze();
            return true;
        }
        catch (Throwable t) {
            return false;
        }
    }
    
    @Override
    public final void freezeIfUnfrozen() throws IllegalStateException {
        if (this.isFrozen()) {
            return;
        }
        this.freeze();
    }
}
