package com.strobel.core;

import java.lang.reflect.*;
import java.util.concurrent.atomic.*;

public final class Closeables
{
    private static final SafeCloseable EMPTY;
    
    static {
        EMPTY = new SafeCloseable() {
            @Override
            public void close() {
            }
        };
    }
    
    public static SafeCloseable empty() {
        return Closeables.EMPTY;
    }
    
    public static SafeCloseable create(final Runnable delegate) {
        return new AnonymousCloseable(VerifyArgument.notNull(delegate, "delegate"), null);
    }
    
    public static void close(final AutoCloseable closeable) {
        try {
            closeable.close();
        }
        catch (Error | RuntimeException e) {
            throw e;
        }
        catch (Throwable t) {
            throw new UndeclaredThrowableException(t);
        }
    }
    
    public static void close(final AutoCloseable... closeables) {
        for (final AutoCloseable closeable : closeables) {
            close(closeable);
        }
    }
    
    public static void tryClose(final AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            }
            catch (Exception loc_0) {}
        }
    }
    
    public static void tryClose(final AutoCloseable... closeables) {
        if (closeables != null) {
            for (final AutoCloseable closeable : closeables) {
                tryClose(closeable);
            }
        }
    }
    
    private static final class AnonymousCloseable implements SafeCloseable
    {
        private static final AtomicIntegerFieldUpdater<AnonymousCloseable> CLOSED_UPDATER;
        private final Runnable _delegate;
        private volatile int _closed;
        
        static {
            CLOSED_UPDATER = AtomicIntegerFieldUpdater.newUpdater(AnonymousCloseable.class, "_closed");
        }
        
        private AnonymousCloseable(final Runnable delegate) {
            super();
            this._delegate = VerifyArgument.notNull(delegate, "delegate");
        }
        
        @Override
        public void close() {
            if (AnonymousCloseable.CLOSED_UPDATER.getAndSet(this, 1) == 0) {
                this._delegate.run();
            }
        }
    }
}
