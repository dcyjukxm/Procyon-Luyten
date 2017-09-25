package com.strobel.concurrent;

import java.util.concurrent.locks.*;
import com.strobel.annotations.*;

public final class StripedReentrantLock extends StripedLock<ReentrantLock>
{
    private static final StripedReentrantLock INSTANCE;
    
    static {
        INSTANCE = new StripedReentrantLock();
    }
    
    public static StripedReentrantLock instance() {
        return StripedReentrantLock.INSTANCE;
    }
    
    public StripedReentrantLock() {
        super(ReentrantLock.class);
    }
    
    @NotNull
    @Override
    protected final ReentrantLock createLock() {
        return new ReentrantLock();
    }
    
    @Override
    public final void lock(final int index) {
        ((ReentrantLock[])this.locks)[index].lock();
    }
    
    @Override
    public final void unlock(final int index) {
        ((ReentrantLock[])this.locks)[index].unlock();
    }
}
