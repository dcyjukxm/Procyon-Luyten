package com.strobel.concurrent;

import java.lang.reflect.*;
import com.strobel.annotations.*;

public abstract class StripedLock<T>
{
    private static final int LOCK_COUNT = 256;
    protected final T[] locks;
    private int _lockAllocationCounter;
    
    protected StripedLock(final Class<T> lockType) {
        super();
        this.locks = (Object[])Array.newInstance(lockType, 256);
        for (int i = 0; i < this.locks.length; ++i) {
            this.locks[i] = this.createLock();
        }
    }
    
    @NotNull
    public T allocateLock() {
        return (T)this.locks[this.allocateLockIndex()];
    }
    
    public int allocateLockIndex() {
        return this._lockAllocationCounter = (this._lockAllocationCounter + 1) % 256;
    }
    
    @NotNull
    protected abstract T createLock();
    
    public abstract void lock(final int param_0);
    
    public abstract void unlock(final int param_0);
}
